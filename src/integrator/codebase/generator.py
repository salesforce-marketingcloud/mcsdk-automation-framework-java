import os
from mcsdk.codebase import generator
from mcsdk.integration.os.process import Command
from mcsdk.integration.os.utils import chdir


class CodeGenerator(generator.AbstractGenerator):
    """ Handles the Swagger codegen process but also custom generation processes """

    def generate_sdk(self):
        """ Generates the SDK code using the swagger codegen library """
        cmd = ' '.join([
            'java',
            '-jar',
            '{swagger_exec}'.format(swagger_exec=self._config['repos']['core']['swagger_cli']),
            'generate',
            '-l',
            'java',
            '-i',
            '{spec_file}'.format(spec_file=self._config['repos']['core']['swagger_spec']),
            '-t',
            '{templates_dir}'.format(templates_dir=os.path.join(self._templates_dir, 'mustache')),
            '-c',
            '{config_file}'.format(config_file=os.path.join(self._config_dir, 'swagger-codegen-config.json')),
            '-o',
            '{sdk_folder}'.format(sdk_folder=self._repo_dir)
        ])

        command = Command(cmd)
        command.run()

        print(command.get_output())

        return not command.returned_errors()

    def build_client_generator(self):
        chdir(os.path.join(self._root_dir, 'src', 'generator', self._config['generators']['java']))
        print('Building client generator...')
        cmd = 'mvn package'

        command = Command(cmd)
        command.run()

        print(command.get_output())

        return not command.returned_errors()

    def generate_client(self):
        """ Generates the SDK codebase custom Client class """
        chdir(os.path.join(self._root_dir, 'src', 'generator', self._config['generators']['java'], 'target'))

        cmd = 'java -jar {client_generator_jar_name} {java_sdk_api_folder}'.format(
            client_generator_jar_name=self._config['generators']['jarName'],
            java_sdk_api_folder=os.path.join(self._repo_dir, self._config["repos"]["sdk"]["apiFolderPath"]))

        command = Command(cmd)
        command.run()

        print(command.get_output())

        return not command.returned_errors()

    def generate(self):
        """ Generates the SDK code """
        if self.generate_sdk() and self.build_client_generator() and self.generate_client():
            return 0

        return 255
