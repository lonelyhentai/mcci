from setuptools import setup, find_packages
import subprocess


# check nodejs dependency
subprocess.run(['node', '--version'])


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
      install_requires=['numpy', 'pygraphviz', 'pandas', 'causality'],
      )
