package br.unb.cic.goda.model;

import br.unb.cic.goda.rtgoretoprism.model.kl.Const;

import java.util.LinkedList;
import java.util.List;

public class FormulaTreeNode {
    public String id;
    public String formula;
    public String annotation;
    public Const decomposition;
    public boolean hasContext;
    public List<FormulaTreeNode> subNodes = new LinkedList<>();
}