package org.htlleo.logic;

import org.htlleo.pattern.Observable;

public class Share extends Observable {
    private String name;
    private double value;
    private int delay = 500;
    private boolean running = false;

    public String getName() {
        return name;
    }
    public double getValue() {
        return value;
    }
    protected void setValue(double value) {
        this.value = value;
    }

    public Share(String name, double startValue) {
        this.name = name;
        this.value = startValue;
    }

    public void start() {
        if (running == false)
        {
            running = true;
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (running) {
                        int sign = ((int)(Math.random() * 100)) % 2;
                        double variation = (Math.random() * 5.0);

                        if (sign > 0) {
                            value = value + (value / 100.0) * variation;
                        }
                        else {
                            value = value - (value / 100.0) * variation;
                        }
                        value = value < 0 ? 0 : value;
                        Share.this.notifyAll(value);
                        try {
                            Thread.sleep(delay);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            t.setDaemon(true);
            t.start();
        }
    }
    public void stop() {
        running = false;
    }
}
