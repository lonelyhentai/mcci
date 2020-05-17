"use strict";
const dagitty = require('./dagitty/dagitty-node');

const GraphAnalyzer = dagitty.GraphAnalyzer;
const GraphParser = dagitty.GraphParser;

module.exports = class CausalModel {
    constructor(dotStr, source, target, unobserveds) {
        this.g = GraphParser.parseDot(dotStr);
        if(source) {
            this.source = source;
            this.setSource(source);
        } else {
            this.source = this.g.getVerticesWithProperty('source')[0].id;
        }
        if(target) {
            this.target = target;
            this.setTarget(target);
        } else {
            this.target = this.g.getVerticesWithProperty('target')[0].id;
        }
        if(unobserveds) {
            this.setUnobserved(u);
        }
    }

    getCausalPaths() {
        const pathVertices = GraphAnalyzer.properPossibleCausalPaths(this.g);
        const map = new Set();
        for(const v of pathVertices) {
            map.add(v.id);
        }
        const res = [];
        for(const e of this.g.edges) {
            if(map.has(e.v1.id)&&map.has(e.v2.id)) {
                res.push(e);
            }
        }
        return res;
    }

    identify() {
        const paths = this.getCausalPaths();
        const res = [];
        for(const p of paths) {
            const gclone = this.g.clone();
            this.unsetSource(this.source, gclone);
            const v1 = p.v1.id;
            const v2 = p.v2.id;
            this.setSource(v1, gclone);
            this.unsetTarget(this.target, gclone);
            this.setTarget(v2, gclone);
            try {
                const adjustments = GraphAnalyzer
                    .canonicalAdjustmentSet(gclone)
                    .map(s=>s.map(n=>n.id));
                res.push([`${v1}->${v2}`, adjustments.map(a=>a.join(","))]);
            } catch(e) {
                res.push([`${v1}->${v2}`, []]);
            }
        }
        return res;
    }

    instruments() {
        return [...new Set(GraphAnalyzer.conditionalInstruments(this.g.clone()).map(i=>i.map(n=>n.id).filter(s=>s).join(',')))];
    }

    setUnobserved(v, g) {
        (g?g:this.g).addVertexProperty(v, 'latentNode');
    }

    unsetUnobserved(v, g) {
        (g?g:this.g).removeVertexProperty(v, 'latentNode');
    }

    setSource(v, g) {
        (g?g:this.g).addVertexProperty(v, 'source');
    }

    unsetSource(v, g) {
        (g?g:this.g).removeVertexProperty(v, 'source');
    }

    setTarget(v, g) {
        (g?g:this.g).addVertexProperty(v, 'target');
    }

    unsetTarget(v, g) {
        (g?g:this.g).removeVertexProperty(v, 'target');
    }
};