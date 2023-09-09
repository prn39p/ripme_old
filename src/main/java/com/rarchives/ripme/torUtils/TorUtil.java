package com.rarchives.ripme.torUtils;

import com.rarchives.ripme.App;
import com.rarchives.ripme.utils.Utils;

import javax.swing.*;

public class TorUtil {


    public static void startTorGUI(TorController tor, JLabel configUpdateLabel, JButton startTorButton) {
        configUpdateLabel.setText("Starting Tor...");

        tor = new TorController(App.torExecutablePath);
        if(!tor.startUp()){
            tor = null;
            configUpdateLabel.setText("Unable to start and connect to tor.");
            return;
        }

        configUpdateLabel.setText(Utils.getLocalizedString("tor.started"));
        startTorButton.setText(Utils.getLocalizedString("stop.tor"));
    }

    public static void stopTorGUI(TorController tor, JLabel configUpdateLabel, JButton startTorButton) {
        configUpdateLabel.setText("Stopping Tor...");

        tor = new TorController(App.torExecutablePath);
        if(!tor.shutDown()) {
            configUpdateLabel.setText("Unable to shut down tor server.");
            return;
        }

        tor = null;
        configUpdateLabel.setText(Utils.getLocalizedString("tor.not.started"));
        startTorButton.setText(Utils.getLocalizedString("start.tor"));
    }

}
