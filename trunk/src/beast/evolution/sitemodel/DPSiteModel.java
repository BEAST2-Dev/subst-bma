package beast.evolution.sitemodel;

import beast.core.parameter.ParameterList;
import beast.core.parameter.DPPointer;
import beast.core.parameter.RealParameter;
import beast.core.Input;
import beast.core.Description;
import beast.core.CalculationNode;
import beast.evolution.substitutionmodel.JC69;

/**
 * @author Chieh-Hsi Wu
 */
@Description("This class facilitates partition selection by DPP on site rates.")
public class DPSiteModel extends CalculationNode {
        //ParameterList
    public Input<ParameterList> paramListInput = new Input<ParameterList>(
            "paramList",
            "A list of unique parameter values",
            Input.Validate.REQUIRED
    );


    //assignment
    public Input<DPPointer> pointersInput = new Input<DPPointer>(
            "pointers",
            "array which points a set of unique parameter values",
            Input.Validate.REQUIRED
    );

    private SiteModel[] siteModels;
    private DPPointer pointers;
    private ParameterList paramList;
    private boolean[] ratesDirty;
    public void initAndValidate() throws Exception{
        JC69 jc = new JC69();
        pointers = pointersInput.get();
        paramList = paramListInput.get();
        int dim = pointers.getDimension();
        siteModels = new SiteModel[dim];
        for(int i = 0;i < dim; i++){
            SiteModel siteModel = new SiteModel();
            RealParameterWrapper muParameter = new RealParameterWrapper(pointers, i);
            siteModel.initByName(
                    "substModel", jc,
                    "mutationRate", muParameter
            );
            siteModels[i] = siteModel;
        }
        ratesDirty = new boolean [dim];
        
        
    }



    public SiteModel getSiteModel(int index){
        return siteModels[index];
    }

    public int getSiteModelCount(){
        return siteModels.length;
    }

    public boolean[] getSiteDirtiness(){
        return ratesDirty;
    }

    public boolean requriesRecalculation(){
        boolean recalculate = false;
        
        if(paramList.somethingIsDirty()){
            recalculate = true;
            for(int i = 0; i < ratesDirty.length;i++){
                ratesDirty[i] = pointers.isParameterDirty(i);
            }
        }else if (pointers.somethingIsDirty()){
            recalculate = true;
            ratesDirty = new boolean[ratesDirty.length];
            ratesDirty[pointers.getLastDirty()] = true;
        }

        return recalculate;
    }




    class RealParameterWrapper extends RealParameter{
        private int index;
        private DPPointer pointers;
        public RealParameterWrapper(DPPointer pointers, int index){
            this.index = index;
            this.pointers = pointers;
        }

        public void initAndValidate() throws Exception{}

        public Double getValue(){
            return pointers.getParameterValue(index);
        }


    }
}
