from setuptools import setup, find_packages

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
      platforms="any",
      zip_safe=False,
      install_requires=[
            'numpy','pygraphviz', 'jupyterlab', 'pandas',
                        ],
      entry_points={
          'console_scripts': []}
      )