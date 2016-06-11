package com.rapidminer.extension.operator;

import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.tools.LogService;
import com.rapidminer.tools.OperatorService;
import com.sun.xml.internal.bind.annotation.OverrideAnnotationOf;

import java.util.logging.Level;

/**
 * Created by XsiteGmbH on 11.06.16.
 */
public class WeightByProbabilityRatio extends Operator {

    public WeightByProbabilityRatio(OperatorDescription description) {
        super(description);
    }

    @Override
    public void doWork() throws OperatorException {
        LogService.getRoot().log(Level.INFO, "Doing some shiat biatch");
    }
}
