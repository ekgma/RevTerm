#!/usr/bin/env python3

"""
CPAchecker is a tool for configurable software verification.
This file is part of CPAchecker.

Copyright (C) 2007-2014  Dirk Beyer
All rights reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.


CPAchecker web page:
  http://cpachecker.sosy-lab.org
"""

# prepare for Python 3
from __future__ import absolute_import, division, print_function, unicode_literals

import glob
import logging
import os
import platform
import subprocess
import sys

sys.dont_write_bytecode = True  # prevent creation of .pyc files
cpachecker_dir = os.path.join(os.path.dirname(__file__), os.pardir)
for egg in glob.glob(os.path.join(cpachecker_dir, "lib", "python-benchmark", "*.whl")):
    sys.path.insert(0, egg)

from benchexec import __version__
import benchexec.benchexec
import benchexec.model
import benchexec.tools
import benchexec.util
import benchmark.util

# Add ./benchmark/tools to __path__ of benchexec.tools package
# such that additional tool-wrapper modules can be placed in this directory.
benchexec.tools.__path__ = [
    os.path.join(os.path.dirname(__file__), "benchmark", "tools")
] + benchexec.tools.__path__


class Benchmark(benchexec.benchexec.BenchExec):
    """
    An extension of BenchExec for use with CPAchecker
    that supports executing the benchmarks in the VerifierCloud.
    """

    DEFAULT_OUTPUT_PATH = "test/results/"

    def create_argument_parser(self):
        parser = super(Benchmark, self).create_argument_parser()
        vcloud_args = parser.add_argument_group("Options for using VerifierCloud")
        vcloud_args.add_argument(
            "--cloud",
            dest="cloud",
            action="store_true",
            help="Use VerifierCloud to execute benchmarks.",
        )

        vcloud_args.add_argument(
            "--cloudMaster",
            dest="cloudMaster",
            metavar="HOST",
            help="Sets the master host of the VerifierCloud instance to be used. If this is a HTTP URL, the web interface is used.",
        )

        vcloud_args.add_argument(
            "--cloudPriority",
            dest="cloudPriority",
            metavar="PRIORITY",
            help="Sets the priority for this benchmark used in the VerifierCloud. Possible values are IDLE, LOW, HIGH, URGENT.",
        )

        vcloud_args.add_argument(
            "--cloudCPUModel",
            dest="cpu_model",
            type=str,
            default=None,
            metavar="CPU_MODEL",
            help="Only execute runs in the VerifierCloud on CPU models that contain the given string.",
        )

        vcloud_args.add_argument(
            "--cloudUser",
            dest="cloudUser",
            metavar="USER:PWD",
            help="The user and password for the VerifierCloud (if using the web interface).",
        )

        vcloud_args.add_argument(
            "--revision",
            dest="revision",
            metavar="(tags/<tag name>|branch_name)[:(HEAD|head|<revision number>)]",
            default="trunk:HEAD",
            help="The svn revision of CPAchecker to use (if using the web interface of the VerifierCloud).",
        )

        vcloud_args.add_argument(
            "--justReprocessResults",
            dest="reprocessResults",
            action="store_true",
            help="Do not run the benchmarks. Assume that the benchmarks were already executed in the VerifierCloud and the log files are stored (use --startTime to point the script to the results).",
        )

        vcloud_args.add_argument(
            "--cloudClientHeap",
            dest="cloudClientHeap",
            metavar="MB",
            default=100,
            type=int,
            help="The heap-size (in MB) used by the VerifierCloud client. A too small heap-size may terminate the client without any results.",
        )

        vcloud_args.add_argument(
            "--cloudSubmissionThreads",
            dest="cloud_threads",
            default=5,
            type=int,
            help="The number of threads used for parallel run submission (if using the web interface of the VerifierCloud).",
        )

        vcloud_args.add_argument(
            "--cloudPollInterval",
            dest="cloud_poll_interval",
            metavar="SECONDS",
            default=5,
            type=int,
            help="The interval in seconds for polling results from the server (if using the web interface of the VerifierCloud).",
        )
        vcloud_args.add_argument(
            "--zipResultFiles",
            dest="zipResultFiles",
            action="store_true",
            help="Packs all result files on the worker into a zip file before file transfer (add this flag if a large number of result files is generated).",
        )

        return parser

    def load_executor(self):
        webclient = False
        if self.config.cloud:
            if self.config.cloudMaster and "http" in self.config.cloudMaster:
                webclient = True
                import benchmark.webclient_benchexec as executor
            else:
                import benchmark.vcloud as executor
            logging.debug(
                "This is CPAchecker's benchmark.py (based on benchexec %s) "
                "using the VerifierCloud %s API.",
                __version__,
                "HTTP" if webclient else "internal",
            )
        else:
            executor = super(Benchmark, self).load_executor()

        if not webclient:
            original_load_function = benchexec.model.load_tool_info

            def build_cpachecker_before_load(tool_name, *args, **kwargs):
                if tool_name == "cpachecker":
                    # This duplicates the logic from our tool-info module,
                    # but we cannot call it here.
                    # Note that base_dir can be different from cpachecker_dir!
                    script = benchexec.util.find_executable("cpa.sh", "scripts/cpa.sh")
                    base_dir = os.path.join(os.path.dirname(script), os.path.pardir)
                    build_file = os.path.join(base_dir, "build.xml")
                    if os.path.exists(build_file) and subprocess.call(
                        ["ant", "-q", "jar"],
                        cwd=base_dir,
                        shell=benchmark.util.is_windows(),
                    ):
                        sys.exit(
                            "Failed to build CPAchecker, please fix the build first."
                        )

                return original_load_function(tool_name, *args, **kwargs)

            # Monkey-patch BenchExec to build CPAchecker before loading the tool-info
            # module (https://gitlab.com/sosy-lab/software/cpachecker/issues/549)
            benchexec.model.load_tool_info = build_cpachecker_before_load

        return executor

    def check_existing_results(self, benchmark):
        if not self.config.reprocessResults:
            super(Benchmark, self).check_existing_results(benchmark)


if __name__ == "__main__":
    benchexec.benchexec.main(Benchmark())
