'use strict';
const CausalModel = require('./causal_model');
const fs = require('fs');

const dotPathArgIndex = process.argv.indexOf('--dot');
if (dotPathArgIndex === -1 || process.argv.length < dotPathArgIndex + 2) {
    throw new SyntaxError("dot path argument not found");
}
const dotPath = process.argv[dotPathArgIndex + 1];
const modeArgIndex = process.argv.indexOf('--mode');
if (modeArgIndex === -1 || process.argv.length < modeArgIndex + 2) {
    throw new SyntaxError("mode argument `--mode <mode>` not found");
}
const mode = process.argv[modeArgIndex + 1];
if (mode !== "identify" && mode !== "instrument") {
    throw new SyntaxError("mode must be identify or instrument");
}

let path = null;
if(mode==="identify") {
    const pathArgIndex = process.argv.indexOf('--path');
    if (process.argv.length < pathArgIndex + 2) {
        throw new SyntaxError("path argument not found when use '--path <path>'");
    }
    path = process.argv[pathArgIndex + 1];
}

const data = fs.readFileSync(dotPath, {encoding: 'utf-8'});
const content = data.toString().replace(/^\s*digraph/, 'dag');
const model = new CausalModel(content);
if (mode === "identify") {
    console.log(JSON.stringify(model.identify(path)));
} else {
    console.log(JSON.stringify(model.instruments()));
}