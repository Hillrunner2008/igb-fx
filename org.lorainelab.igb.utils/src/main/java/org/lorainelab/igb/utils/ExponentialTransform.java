package org.lorainelab.igb.utils;

public class ExponentialTransform {

    private final double lxmin, ratio;

    public ExponentialTransform(double xScaleMin, double xScaleMax) {
        double lxmax = Math.log(xScaleMax);
        lxmin = Math.log(xScaleMin);
        ratio = (lxmax - lxmin) / 100;
    }

    public double transform(double in) {
        double out = Math.exp(in * ratio + lxmin);
        /*
         *  Fix for zooming -- for cases where y _should_ be 7, but ends up
         *  being 6.9999998 or thereabouts because of errors in Math.exp()
         */
        if (Math.abs(out) > .1) {
            double outround = Math.round(out);
            if (Math.abs(out - outround) < 0.0001) {
                out = outround;
            }
        }
        return out;
    }

    public double inverseTransform(double in) {
        return (Math.log(in) - lxmin) / ratio;
    }
}
