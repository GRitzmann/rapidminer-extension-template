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
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeString;
import com.rapidminer.tools.LogService;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;


public class WeightByDocumentFrequency extends AbstractWeighting {

    static double numberExamples; //muss be a binary occurence vector

    /**
     * The parameter name for &quot;The target class for which to find characteristic feature
     * weights.&quot;
     */

    public WeightByDocumentFrequency(OperatorDescription description) {

        super(description, true);
        // TODO: Add Dictionary Quickfix for parameter.
    }

    @Override
    protected AttributeWeights calculateWeights(ExampleSet es) throws OperatorException {

        AttributeWeights attWeights = new AttributeWeights();
        try {
            double[] weights = generateWeightsForClass(es);

            LogService.getRoot().log(Level.INFO, "Length weight: "+ weights.length +" should be " + es.size());
            numberExamples = es.size();

            double maxWeight = Double.NEGATIVE_INFINITY;
            for (double w : weights) {
                maxWeight = Math.max(maxWeight, w);
            }
            LogService.getRoot().log(Level.INFO, "MAX Weight :" + maxWeight);

            int i = 0;

            for (Attribute attribute : es.getAttributes()) {
                double df = weights[i];
                attWeights.setWeight(attribute.getName(), df);
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


    private double[] generateWeightsForClass(ExampleSet exampleSet) {

        double[] termFrequencySum = new double[exampleSet.size()];
        List<Attribute> attributes = new LinkedList<Attribute>();
        for (Attribute attribute : exampleSet.getAttributes()) {
            if (attribute.isNumerical()) {
                attributes.add(attribute);
            }
        }
        double[] documentFrequencies = new double[attributes.size()];

        // calculate frequencies
        int exampleCounter = 0;
        for (Example example : exampleSet) {
            int i = 0;
            for (Attribute attribute : attributes) {
                double value = example.getValue(attribute);
                termFrequencySum[exampleCounter] += value;
                if (value > 0) {
                    documentFrequencies[i]++;
                }
                i++;
            }
            exampleCounter++;
        }
        return documentFrequencies;
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

}

