package br.unb.cic.integration;

import br.unb.cic.goda.model.FormulaTreeModel;
import br.unb.cic.goda.model.FormulaTreeNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import br.unb.cic.goda.model.ModelTypeEnum;

@RestController
public class IntegrationController {

	private IntegrationService service;

	@Autowired
    public IntegrationController(IntegrationService integrationService) {
	    this.service = integrationService;
    }

	@RequestMapping(value = "/prism/MDP", method = RequestMethod.POST)
    public void prismMDP( @RequestParam(value = "content") String content) {
		this.service.executePrism(content, ModelTypeEnum.MDP.getTipo(), "src/main/webapp/prism.zip");
    }
	
	@RequestMapping(value = "/prism/DTMC", method = RequestMethod.POST)
    public void prismDTMC( @RequestParam(value = "content") String content) {
		this.service.executePrism(content, ModelTypeEnum.DTMC.getTipo(), "src/main/webapp/prism.zip");
    }
	
	@RequestMapping(value = "/param/DTMC", method = RequestMethod.POST)
    public void paramDTMC( @RequestParam(value = "content") String content) {
		this.service.executeParam(content, ModelTypeEnum.PARAM.getTipo(), true, "src/main/webapp/param.zip");
    }
	
    @RequestMapping(value = "/epmc/DTMC", method = RequestMethod.POST)
    public void epmcDTMC(@RequestParam(value = "content") String content) {
    	this.service.executeParam(content, ModelTypeEnum.EPMC.getTipo(), false, "src/main/webapp/epmc.zip");
    }

	@RequestMapping(value = "/param/MDP", method = RequestMethod.POST)
    public void paramMDP( @RequestParam(value = "content") String content) {
		this.service.executeParam(content, ModelTypeEnum.PARAM.getTipo(), true, "src/main/webapp/param.zip");
    }
	
    @RequestMapping(value = "/epmc/MDP", method = RequestMethod.POST)
    public void epmcMDP(@RequestParam(value = "content") String content) {
    	this.service.executeParam(content, ModelTypeEnum.EPMC.getTipo(), false, "src/main/webapp/epmc.zip");
    }

    @RequestMapping(value = "/formula/reliability", method = RequestMethod.GET)
    public @ResponseBody String getReliabilityFormulaTree(@RequestParam(value = "id") String id, @RequestParam(value = "goal") String goal) {
	    return this.service.getReliabilityFormulaTree(id, goal);
    }

    @RequestMapping(value = "/formula/cost", method = RequestMethod.GET)
    public @ResponseBody String getCostFormulaTree(@RequestParam(value = "id") String id, @RequestParam(value = "goal") String goal) {
	    return this.service.getCostFormulaTree(id, goal);
    }

    @RequestMapping(value = "/formula/reliability", method = RequestMethod.PUT)
    public @ResponseBody String editReliabilityFormulaTree(
            @RequestParam(value = "id") String id,
            @RequestParam(value = "goal") String goal,
            @RequestBody FormulaTreeNode newSubTree) {

	    return this.service.editFormulaTree(id, goal, newSubTree, true);
    }

    @RequestMapping(value = "/formula/cost", method = RequestMethod.PUT)
    public @ResponseBody String editCostFormulaTree(
            @RequestParam(value = "id") String id,
            @RequestParam(value = "goal") String goal,
            @RequestBody FormulaTreeNode newSubTree) {

        return this.service.editFormulaTree(id, goal, newSubTree, false);
    }

    @RequestMapping(value = "/formula/reliability", method = RequestMethod.POST)
    public @ResponseBody FormulaTreeModel createReliabilityFormulaTree(@RequestBody FormulaTreeModel model) {
	    return this.service.createFormulaTree(model, true);
    }

    @RequestMapping(value = "/formula/cost", method = RequestMethod.POST)
    public @ResponseBody FormulaTreeModel createCostFormulaTree(@RequestBody FormulaTreeModel model) {
        return this.service.createFormulaTree(model, false);
    }

    @RequestMapping(value = "/formula/reliability", method = RequestMethod.DELETE)
    public @ResponseBody FormulaTreeNode deleteReliabilityFormulaTree(
        @RequestParam(value = "id") String id,
        @RequestParam(value = "goal") String goal) {

	    return this.service.deleteFormulaTree(id, goal, true);
    }

    @RequestMapping(value = "/formula/cost", method = RequestMethod.DELETE)
    public @ResponseBody FormulaTreeNode deleteCostFormulaTree(
            @RequestParam(value = "id") String id,
            @RequestParam(value = "goal") String goal) {

        return this.service.deleteFormulaTree(id, goal, false);
    }
}