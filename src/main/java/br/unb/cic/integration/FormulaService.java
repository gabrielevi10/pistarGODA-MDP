package br.unb.cic.integration;

import br.unb.cic.goda.model.FormulaTreeNode;
import br.unb.cic.goda.rtgoretoprism.generator.goda.writer.ManageWriter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

@Service
public class FormulaService {
    public String getReliabilityFormulaTree(String id, String goal) {
        FormulaTreeNode formulaTree = loadFormulaTreeFromJson(id, goal, true);

        return parseObjectToJsonString(formulaTree);
    }

    public String getCostFormulaTree(String id, String goal) {
        FormulaTreeNode formulaTree = loadFormulaTreeFromJson(id, goal, false);

        return parseObjectToJsonString(formulaTree);
    }

    private FormulaTreeNode loadFormulaTreeFromJson(String id, String goal, boolean isReliability) {
        try {
            FormulaTreeNode formulaTree = loadFormulaTreeFromJson(id, isReliability);
            return getFormulaSubTree(formulaTree, goal);
        } catch(Exception e) {
            return new FormulaTreeNode();
        }
    }

    private FormulaTreeNode loadFormulaTreeFromJson(String id, boolean isReliability) {
        FormulaTreeNode formulaTree = new FormulaTreeNode();
        try {
            String content;
            if (isReliability) {
                content = ManageWriter.readFileAsString("resources/reliability/" + id + "_reliability.json");
            } else {
                content = ManageWriter.readFileAsString("resources/cost/" + id + "_cost.json");
            }

            ObjectMapper objectMapper = new ObjectMapper();
            formulaTree = objectMapper.readValue(content, FormulaTreeNode.class);

            return formulaTree;
        } catch(Exception e) {
            return formulaTree;
        }
    }

    private FormulaTreeNode getFormulaSubTree(FormulaTreeNode formulaTreeNode, String goal) {
        Queue<FormulaTreeNode> formulaTreeNodeQueue = new LinkedList<>();
        Set<String> visited = new HashSet<>();
        FormulaTreeNode node = null;

        formulaTreeNodeQueue.add(formulaTreeNode);
        visited.add(formulaTreeNode.id);

        while (!formulaTreeNodeQueue.isEmpty()) {
            node = formulaTreeNodeQueue.remove();
            if (node.id.equals(goal)) {
                break;
            } else {
                node.subNodes.forEach(subNode -> {
                    if (!visited.contains(subNode.id)) {
                        visited.add(subNode.id);
                        formulaTreeNodeQueue.add((subNode));
                    }
                });
            }
        }

        return node;
    }

    public String editFormulaTree(String id, String goal, FormulaTreeNode subTree, boolean isReliability) {
        FormulaTreeNode formulaTree = loadFormulaTreeFromJson(id, isReliability);
        updateFormulaTree(id, goal, formulaTree, subTree);

        String json = parseObjectToJsonString(formulaTree);

        try {
            writeFormulaTreeJson(id, json, isReliability);
        } catch (IOException e) {
            return "";
        }

        return json;
    }

    private void updateFormulaTree(String id, String goal, FormulaTreeNode formulaTree, FormulaTreeNode subTree) {
        FormulaTreeNode formulaTreeToEdit = getFormulaSubTree(formulaTree, goal);

        String oldFormula = formulaTreeToEdit.formula;

        formulaTreeToEdit.id = subTree.id;
        formulaTreeToEdit.formula = subTree.formula;
        formulaTreeToEdit.subNodes = subTree.subNodes;

        updateFormulaOnTree(formulaTree, oldFormula, subTree.formula);
    }

    private void updateFormulaOnTree(FormulaTreeNode formulaTree, String oldFormula, String newFormula) {
        formulaTree.formula = formulaTree.formula.replace(oldFormula, newFormula);

        formulaTree.subNodes.forEach(node -> {
            updateFormulaOnTree(node, oldFormula, newFormula);
        });
    }

    private void writeFormulaTreeJson(String id, String json, boolean isReliability) throws IOException {
        PrintWriter printWriter;

        if (isReliability) {
            printWriter = new PrintWriter("resources/reliability/" + id + "_reliability.json");
        } else {
            printWriter = new PrintWriter("resources/cost/" + id + "_cost.json");
        }

        ManageWriter.printModel(printWriter, json);
    }

    private String parseObjectToJsonString(Object object) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            return "";
        }
    }
}
