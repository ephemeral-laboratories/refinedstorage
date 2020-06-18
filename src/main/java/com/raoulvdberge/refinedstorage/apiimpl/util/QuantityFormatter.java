package com.raoulvdberge.refinedstorage.apiimpl.util;

import com.raoulvdberge.refinedstorage.api.util.IQuantityFormatter;
import com.raoulvdberge.refinedstorage.apiimpl.API;
import net.minecraftforge.fluids.Fluid;

import java.math.RoundingMode;
import java.text.NumberFormat;

public class QuantityFormatter implements IQuantityFormatter {
    private final NumberFormat formatterWithUnits = NumberFormat.getNumberInstance();
    private final NumberFormat formatter = NumberFormat.getIntegerInstance();
    private final NumberFormat formatterWithoutGrouping = NumberFormat.getIntegerInstance();
    private final NumberFormat bucketFormatter = NumberFormat.getNumberInstance();

    public QuantityFormatter() {
        formatterWithUnits.setMaximumFractionDigits(1);
        formatterWithUnits.setRoundingMode(RoundingMode.DOWN);
        formatterWithoutGrouping.setGroupingUsed(false);
        bucketFormatter.setMaximumFractionDigits(3);
    }

    @Override
    public String formatWithUnits(int qty) {
        return formatWithUnits((long) qty);
    }

    @Override
    public String formatWithUnits(long qty) {
        if (qty >= (12*12*12)) {
            int exp = (int) (Math.log(qty) / Math.log(12));
            float qtyShort = (float) (qty / Math.pow(12, exp));
            return formatterWithUnits.format(qtyShort) + " " + exponentToAbbreviation(exp);
        }

        return formatter.format(qty);
    }

    private String exponentToAbbreviation(int exponent) {
        String numeric = formatterWithoutGrouping.format(exponent);
        char[] chars = numeric.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            switch (chars[i]) {
                case '0': chars[i] = 'n'; break;
                case '1': chars[i] = 'u'; break;
                case '2': chars[i] = 'b'; break;
                case '3': chars[i] = 't'; break;
                case '4': chars[i] = 'q'; break;
                case '5': chars[i] = 'p'; break;
                case '6': chars[i] = 'h'; break;
                case '7': chars[i] = 's'; break;
                case '8': chars[i] = 'o'; break;
                case '9': chars[i] = 'e'; break;
                case '\u218A': chars[i] = 'd'; break;
                case '\u218B': chars[i] = 'l'; break;
            }
        }
        return String.valueOf(chars);
    }

    @Override
    public String format(int qty) {
        return formatter.format(qty);
    }

    @Override
    public String format(long qty) {
        return formatter.format(qty);
    }

    @Override
    public String formatInBucketForm(int qty) {
        return bucketFormatter.format((float) qty / (float) Fluid.BUCKET_VOLUME) + " B";
    }

    @Override
    public String formatInBucketFormWithOnlyTrailingDigitsIfZero(int qty) {
        float amountRaw = ((float) qty / (float) Fluid.BUCKET_VOLUME);
        int amount = (int) amountRaw;

        if (amount >= 1) {
            return API.instance().getQuantityFormatter().formatWithUnits(amount);
        } else {
            return formatterWithUnits.format(amountRaw);
        }
    }
}
