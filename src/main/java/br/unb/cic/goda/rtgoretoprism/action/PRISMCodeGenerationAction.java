package br.unb.cic.goda.rtgoretoprism.action;

import java.io.IOException;
import java.util.Set;

import br.unb.cic.goda.model.Actor;
import br.unb.cic.goda.rtgoretoprism.generator.CodeGenerationException;
import br.unb.cic.goda.rtgoretoprism.generator.goda.producer.RTGoreProducer;

public class PRISMCodeGenerationAction {

    private Set<Actor> selectedActors;
    private Set<Goal> selectedGoals;
    private String typeModel;

    public PRISMCodeGenerationAction(Set<Actor> selectedActors, Set<Goal> selectedGoals, String typeModel) {
        this.selectedActors = selectedActors;
        this.selectedGoals = selectedGoals;
        this.typeModel = typeModel;
    }

    public void run() {
        if (selectedActors.isEmpty())
            return;
        String sourceFolder = "src/main/resources/TemplateInput";
        String targetFolder = "dtmc";
        RTGoreProducer producer = new RTGoreProducer(selectedActors, selectedGoals, typeModel, sourceFolder, targetFolder);
        try {
            producer.run();
        } catch (CodeGenerationException | IOException e) {
            e.printStackTrace();
        }
    }

}