from setuptools import setup, find_packages
from subprocess import Popen

# check nodejs dependency
with Popen(['node', '--version']) as process:
    process.wait()
    if process.returncode != 0:
        raise EnvironmentError("use have node installed")

setup(name='libmcci',
      version='0.1.0',
      description='Causal inference library of minecraft',
      url='http://github.com/lonelyhentai/mcci',
      author='Zhou Yeheng',
      author_email='master@evernightfireworks.com',
      license='MIT',
      keywords=('minecraft', 'mcci', 'casual inference'),
      packages=find_packages(),
      include_package_data=True,
      package_data={"": ["*.js"]},
      platforms="any",
      zip_safe=False,
      install_requires=['numpy', 'pygraphviz', 'pandas', 'scikit-learn'],
      )
