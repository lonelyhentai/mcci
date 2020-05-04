# LIBMCCI

## Create new python env and activate

Create your virtual env, and activate your env and install wheel, numpy.

```bash
python -m venv libmcci-env
# for *nix
source libmcci-env/Scripts/activate
# for windows cmd (please do not use powershell, because you should activate vcvars64.bat)
libmcci-env/Scripts/activate.bat
```

## Install jep and dowhy

First install build tools, for using `numpy` with `jep`, please install it

```bash
pip install wheel numpy
```

Install the `jep` and `dowhy`:

- If you are using windows, please ensure you have `visual studio build tools` and `windows SDK` 
of required version of python installed. And you should have `vcvars64.bat` script in your path,
then using `cmd` rather than `powershell`.
- And for *nix, ensure you have gcc or clang installed.

```bash
vcvars64.bat # just for windows
pip install jep dowhy
```
