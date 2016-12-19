package simulator.reaction;

import simulator.Simulator;
import simulator.SoluteGrid;
import simulator.SpatialGrid;
import simulator.reaction.kinetic.IsCompositionFactor;
import simulator.reaction.kinetic.IsKineticFactor;
import utils.XMLParser;

/**
 * Created by chris on 6/25/2015.
 */
public class SoluteComposition {


    private boolean      _noAccumulation;
    private SpatialGrid  _massGrid;
    private SoluteGrid[] _allSolutes;
    private int          _outputSoluteIndex;
    private int _inputSoluteIndex = -1;
    private IsKineticFactor _function;


    public void init(Simulator aSim, XMLParser aCompositionRoot, String domainName) {


        _massGrid = aSim.world.getDomain(domainName).getBiomass();
        _allSolutes = aSim.soluteList;
        _outputSoluteIndex = findSoluteIndexByName(aCompositionRoot.getAttribute("yieldSolute"), domainName);

        _noAccumulation = aCompositionRoot.getParamBool("NoAccumulation");

        XMLParser fucntionElement = aCompositionRoot.getChild("function");

        String soluteName = fucntionElement.getAttribute("soluteParticle");
        _function = (IsKineticFactor) fucntionElement.instanceCreator("simulator.reaction.kinetic");

        _function.init(fucntionElement.getElement());


        if (!soluteName.equalsIgnoreCase("biomass") && !soluteName.equalsIgnoreCase("mass"))
            _inputSoluteIndex = findSoluteIndexByName(soluteName, domainName);

    }


    private int findSoluteIndexByName(String name, String domainName) {
        for (int i = 0; i < _allSolutes.length; i++) {
            if (_allSolutes[i].getName().equalsIgnoreCase(name) && _allSolutes[i].getDomain().getName().equalsIgnoreCase(domainName))
                return i;
        }
        return -1;
    }


    public void compose(boolean is3D) {
        if (_inputSoluteIndex == -1) {

            compose(is3D, _noAccumulation, _allSolutes[_outputSoluteIndex], _massGrid, _function);
        } else {
            compose(is3D, _noAccumulation, _allSolutes[_outputSoluteIndex], _allSolutes[_inputSoluteIndex], _function);
        }
    }

    private static void compose(boolean is3D, boolean noAccumulation, SpatialGrid output, SpatialGrid input, IsKineticFactor function) {
        int    k;
        for (int i = 1; i <= output.getGridSizeI(); i++) {
            for (int j = 1; j <= output.getGridSizeJ(); j++) {
                if (is3D) {
                    for (k = 1; k <= output.getGridSizeK(); k++) {
                        ((IsCompositionFactor) function).compose(noAccumulation, output, input, i, j, k);

                    }
                } else {
                    k = ((output.getGridSizeK() + 1) / 2);
                    ((IsCompositionFactor) function).compose(noAccumulation, output, input, i, j, k);

                }
            }
        }
    }




}
