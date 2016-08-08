package com.rapidminer.extension.operator;

/**
 * Copyright (C) 2001-2016 by RapidMiner and the contributors
 *
 * Complete list of developers available at our web site:
 *
 * http://rapidminer.com
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see http://www.gnu.org/licenses/.
 */

import com.rapidminer.example.Attribute;
import com.rapidminer.example.AttributeWeights;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.OperatorCapability;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.features.weighting.AbstractWeighting;
import com.rapidminer.parameter.*;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.List;

import java.util.logging.Level;

import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeString;
import com.rapidminer.tools.LogService;


public class WeightByProbabilityRatioOperator extends AbstractWeighting {

    static double pos; //number of examples within target class (e.g. 0 or 1)
    static double numberExamples;

    /**
     * The parameter name for &quot;The target class for which to find characteristic feature
     * weights.&quot;
     */
    private static final String PARAMETER_CLASS_TO_CHARACTERIZE = "class_to_characterize";

    public WeightByProbabilityRatioOperator(OperatorDescription description) {

        super(description, true);
        // TODO: Add Dictionary Quickfix for parameter.
    }

    @Override
    protected AttributeWeights calculateWeights(ExampleSet es) throws OperatorException {

        AttributeWeights attWeights = new AttributeWeights();
        try {
            String targetValue = getParameterAsString(PARAMETER_CLASS_TO_CHARACTERIZE);

            double[] weightsPos = generateWeightsForClass(es, targetValue);
            LogService.getRoot().log(Level.INFO, "Length weightsPos: "+ weightsPos.length);

            double[] weightsNeg = generateWeightsForClass(es, "0");

            LogService.getRoot().log(Level.INFO, "Length weightsNeg: "+ weightsNeg.length);
            numberExamples = es.size();

            double maxWeight = Double.NEGATIVE_INFINITY;
            for (double w : weightsPos) {
                maxWeight = Math.max(maxWeight, w);
            }

            LogService.getRoot().log(Level.INFO, "MAX Weight Pos:" + maxWeight);

            int i = 0;
            for (Attribute attribute : es.getAttributes()) {
                double tp = weightsPos[i];
                double fp = weightsNeg[i];
                //if(attribute.getName().equalsIgnoreCase("exchange")){
                    LogService.getRoot().log(Level.INFO, " CALCULATING "+ attribute.getName() +" via: tp="+tp+", fp: "+fp +", neg: "+(numberExamples - pos)+", pos: "+pos);
                //}
                    attWeights.setWeight(attribute.getName(), calculatePR(tp, fp));

                i++;
            }


        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            LogService.getRoot().log(Level.INFO, exceptionAsString);
        }
        finally {
            return attWeights;
        }
    }


    private double calculatePR(double tp, double fp){

        double tpr = (tp/pos);
        double fpr = 1.0d - 0.000000001d;

        if(fp > 0.0d){
           fpr = (fp/(numberExamples-pos));
        }

        // double weight = nominator/ denominator ;
        double weight = tpr / fpr;
        //double weight = (tp/pos)/(fp/numberExamples - pos);
        return weight;
    }

    private double[] generateWeightsForClass(ExampleSet es, String value) {

        double[] result = new double[es.getAttributes().size()];
        for (int i = 0; i < es.getAttributes().size(); i++) {
            result[i] = 0.0;
        }
        Iterator<Example> er = es.iterator();
        Attribute labelAttribute = es.getAttributes().getLabel();

        int elementsInClass = 0;

        while (er.hasNext()) {
            Example e = er.next();
            if (e.getValueAsString(labelAttribute).equalsIgnoreCase(value)) {
                elementsInClass++;
                int index = 0;
                for (Attribute attribute : es.getAttributes()) {
                    result[index] += e.getValue(attribute); //addiert alle Gewichte in der positiven Reihe
                    index++;
                }
                LogService.getRoot().log(Level.INFO,e.getValueAsString(labelAttribute) + ", "+result[index-1]);
            }

        }
        if (!value.equalsIgnoreCase("0")){
            pos = (double) elementsInClass;
        }
        LogService.getRoot().log(Level.INFO, "Found " + elementsInClass + " results in class " + value);
        return result;
    }

    @Override
    public boolean supportsCapability(OperatorCapability capability) {
        switch (capability) {
            case BINOMINAL_LABEL:
            case POLYNOMINAL_LABEL:
            case NUMERICAL_ATTRIBUTES:
                return true;
            default:
                return false;
        }
    }

    @Override
    public List<ParameterType> getParameterTypes() {
        List<ParameterType> types = super.getParameterTypes();
        types.add(new ParameterTypeString(PARAMETER_CLASS_TO_CHARACTERIZE,
                "The target class for which to find characteristic feature weights.", false, false));
        return types;
    }
}

