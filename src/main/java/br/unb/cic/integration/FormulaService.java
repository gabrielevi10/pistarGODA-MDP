package br.unb.cic.integration;

import br.unb.cic.goda.model.FormulaTreeModel;
import br.unb.cic.goda.model.FormulaTreeNode;
import br.unb.cic.goda.rtgoretoprism.generator.goda.producer.PARAMProducer;
import br.unb.cic.goda.rtgoretoprism.generator.goda.writer.ManageWriter;
import br.unb.cic.goda.rtgoretoprism.model.kl.Const;
import br.unb.cic.goda.rtgoretoprism.model.kl.RTContainer;
import br.unb.cic.goda.rtgoretoprism.paramformula.SymbolicParamGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.util.*;


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
            return getFormulaSubTree(formulaTree, goal, false);
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

    private FormulaTreeNode getFormulaSubTree(FormulaTreeNode formulaTreeNode, String goal, boolean parent) {
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
                for (FormulaTreeNode subNode : node.subNodes) {
                    if (parent && subNode.id.equals(goal)) {
                        return node;
                    }

                    if (!visited.contains(subNode.id)) {
                        visited.add(subNode.id);
                        formulaTreeNodeQueue.add((subNode));
                    }
                }
            }
        }

        return node;
    }

    public String editFormulaTree(String id, String goal, FormulaTreeNode subTree, boolean isReliability, boolean shouldPersist) {
        FormulaTreeNode formulaTree = loadFormulaTreeFromJson(id, isReliability);
        updateFormulaTree(id, goal, formulaTree, subTree, isReliability);

        String json = parseObjectToJsonString(formulaTree);

        if (shouldPersist) {
            try {
                writeFormulaTreeJson(id, json, isReliability);
            } catch (IOException e) {
                return "";
            }
        }

        return json;
    }

    private void updateFormulaTree(String id, String goal, FormulaTreeNode formulaTree, FormulaTreeNode subTree, boolean isReliability) {
        FormulaTreeNode formulaTreeToEdit = getFormulaSubTree(formulaTree, goal, false);

        String oldFormula = formulaTreeToEdit.formula;
        Map<String, String> ctxInformation = new HashMap<>();

        List<String> subNodesIds = new ArrayList<>();
        for (FormulaTreeNode node : subTree.subNodes) {
            subNodesIds.add(node.id);
            if (node.hasContext) {
                ctxInformation.put(node.id, "");
            }
        }

        String[] subNodesIdsArr = subNodesIds.toArray(new String[0]);
        PARAMProducer paramProducer = new PARAMProducer(ctxInformation);

        try {
            formulaTreeToEdit.formula = paramProducer.getNodeForm(subTree.decomposition,
                    subTree.annotation, subTree.id, isReliability, subNodesIdsArr);

            for (FormulaTreeNode node : subTree.subNodes) {
                if (isReliability) {
                    if (!formulaTreeToEdit.formula.contains("R_" + node.id)) {
                        formulaTreeToEdit.formula = formulaTreeToEdit.formula.replace(node.id, "R_" + node.id);
                    }
                } else {
                    if (!formulaTreeToEdit.formula.contains("W_" + node.id)) {
                        formulaTreeToEdit.formula = formulaTreeToEdit.formula.replace(node.id, "W_" + node.id);
                    }
                }
            }
        } catch (Exception e) {
            return;
        }

        formulaTreeToEdit.id = subTree.id;
        formulaTreeToEdit.subNodes = subTree.subNodes;
        formulaTreeToEdit.hasContext = subTree.hasContext;
        formulaTreeToEdit.annotation = subTree.annotation;
        formulaTreeToEdit.decomposition = subTree.decomposition;

        updateFormulaOnTree(formulaTree, oldFormula, formulaTreeToEdit.formula);
    }

    private void updateFormulaOnTree(FormulaTreeNode formulaTree, String oldFormula, String newFormula) {
        formulaTree.formula = formulaTree.formula.replace(oldFormula, newFormula);

        formulaTree.subNodes.forEach(node -> updateFormulaOnTree(node, oldFormula, newFormula));
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
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            return "";
        }
    }

    public FormulaTreeModel createFormulaTree(String id, FormulaTreeNode tree, boolean isReliability) {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        long timeId = timestamp.getTime();
        id = id + "_" + timeId;
        FormulaTreeModel model = new FormulaTreeModel();
        try {
            String json = parseObjectToJsonString(tree);
            this.writeFormulaTreeJson(id, json, isReliability);

            model.setId(id);
            model.setTree(tree);
        } catch (IOException ignored) { }

        return model;
    }

    public FormulaTreeNode deleteFormulaTree(String id, String goal, boolean isReliability) {
        FormulaTreeNode formulaTree = loadFormulaTreeFromJson(id, isReliability);
        FormulaTreeNode parentNode = getFormulaSubTree(formulaTree, goal, true);
        String formulaToDelete = "";

        try {
            if (formulaTree.id.equals(goal)) {

                writeFormulaTreeJson(id, "", isReliability);
                return new FormulaTreeNode();
            } else {
                for (FormulaTreeNode node : parentNode.subNodes) {
                    if (node.id.equals(goal)) {
                        formulaToDelete = node.formula;
                        parentNode.subNodes.remove(node);
                        break;
                    }
                }
            }

            updateFormulaOnTree(formulaTree, formulaToDelete, "");
            writeFormulaTreeJson(id, parseObjectToJsonString(formulaTree), isReliability);
        } catch (IOException e) {
            return new FormulaTreeNode();
        }

        return formulaTree;
    }
}
