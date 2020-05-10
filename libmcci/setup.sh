conda create --prefix ./libmcci-env python=3.6 --copy
conda activate ./libmcci-env
pip install wheel numpy
pip install tensorflow==1.*
pip install jupyterlab
python -m ipykernel install --name mcci
pip install dowhy
conda install -c alubbock graphviz pygraphviz
libmcci-env/Scripts/dot -c