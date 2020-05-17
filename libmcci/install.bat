conda create --prefix .\libmcci-env python=3.6 --copy
conda activate .\libmcci-env
conda install -c alubbock graphviz pygraphviz
conda install nodejs
libmcci-env\Scripts\dot -c
libmcci-env\Scripts\pip install jupyterlab
libmcci-env\Scripts\pip install .