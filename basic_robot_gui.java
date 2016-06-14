/*
 * #%L
 * **********************************************************************
 * ORGANIZATION  :  not much
 * PROJECT       :  GoPiGo GUI controller in Java
 * FILENAME      :  basic_robot_gui.java
 * DATE          :  2016-06-14
 * AUTHOR        :  -bob,mon. (bloomu.prof@gmail.com)
 *
 * Act sort of like "basic_robot_gui.py".
 * This file depends on the GoPiGo Java Library project. More information about
 * this project can be found here:  https://github.com/DexterInd/GoPiGo
 * **********************************************************************
 * %%
 * GoPiGo for the Raspberry Pi: an open source robotics platform for the Raspberry Pi.
 * Copyright (C) 2015  Dexter Industries

 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/gpl-3.0.txt>.
 *
 * #L%
 */

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import com.dexterind.gopigo.*;
import com.dexterind.gopigo.components.*;
import com.dexterind.gopigo.events.*;
import com.dexterind.gopigo.utils.Statuses;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

class basic_robot_gui extends JPanel implements KeyListener, GopigoListener {
    private static int ultrasonicPin = 15;
    private String helpMsg =
        "Control keys are chosen to be convenient for a touch typist,\n" +
        "using either the left hand or right hand.\n" +

        "\nHelp:  '?'" +

        "\n\nStop:        [SPACE] bar" +
        "\nForward:     'i'  or  'e'" +
        "\nBackward:    'k'  or  'd'" +
        "\nTurn Right:  'l'  or  'f'" +
        "\nTurn Left:   'j'  or  's'" +

        "\n\nRotate Right:  ';'  or  'g'" +
        "\nRotate Left:   'h'  or  'a'" +
        "\nSpeed up:      '8'  or  '4'" +
        "\nSlow down:     ','  or  'x'" +

        "\n\nToggle left LED:  'q'" +
        "\nToggle right LED: '['" +

        "\n\nQuit:             [Esc], 'X', or 'Q'";

    private String outputMessage = helpMsg;
    private String resultValue = null;

    private Boolean ledLeft = false;
    private Boolean ledRight = false;
    private Boolean released = true;

    private static Gopigo gopigo = null;

    public basic_robot_gui() throws IOException, InterruptedException {
        this.setPreferredSize(new Dimension(500, 200));
        this.setFont(new Font("Verdana", Font.PLAIN, 20));
        addKeyListener(this);

        gopigo = Gopigo.getInstance();
        gopigo.addListener(this);

        gopigo.ultraSonicSensor.setPin(ultrasonicPin);  // currently unused...
        gopigo.setMinVoltage(5.5);
        gopigo.init();
    }

    public void addNotify() {
        super.addNotify();
        requestFocus();
    }

    public void paintComponent(Graphics g) {
        g.clearRect(0, 0, getWidth(), getHeight());
        g.drawString(outputMessage + "->" + resultValue, 20, 100);
    }

  // Event handlers
  public void onStatusEvent(StatusEvent event) {
    System.out.println("\f");
    System.out.println("[Status Changed]");
    switch (event.status) {
      case Statuses.INIT:
        System.out.println("OK Init");
        break;
      case Statuses.HALT:
        System.out.println("WARN Halt");
        break;
    }
  }
    public void onVoltageEvent(VoltageEvent event) {
      System.out.println("\f");
      System.out.println("[Voltage Event]");
      System.out.println(event.value + " Volts");
    }

    /*
    * Slightly tricky logic using the "released" boolean here - I want to
    * fire off an action when a key is first pressed, then ignore the keypress
    * thereafter until the key is released again - at which point the action 
    * should be released.  The boolean produces a primitive, two-state finite
    * state machine.
    * This is intended to get around the issue that keypress events are buffered,
    * and build up faster than the logic can process and dispose of them.
    * With this approach the user experience is similar to that of the python
    * "basic_robot_gui.py" program.
    *
    * (I blame swing, or maybe awt, for not interpreting "keypress" to mean a
    * single action that is followed by a keyrelease.  Instead, "keypress" seems
    * to generate a series of events as fast as it can, as long as the key remains
    * down.)
    */
    public void keyPressed(KeyEvent evt) { }
    public void keyReleased(KeyEvent evt) {
        released = true;
        try {
            outputMessage = "stop";
            resultValue = Integer.toString(gopigo.motion.stop());
        } catch (IOException ex) {
          System.out.println("IO error trying for Key Release!");
          System.exit(1);
        }
    }
    public void keyTyped(KeyEvent evt) {
        if (released) {
            released = false;
            try {
                char c = evt.getKeyChar();
                switch (c) {
                  case '?':
                    outputMessage = helpMsg;
                    resultValue = Integer.toString(gopigo.motion.stop());
                    System.out.println(outputMessage);
                    break;

                  case '\u001b':    // Escape key
                  case 'X': // uppercase X
                  case 'Q': // uppercase Q
                    System.exit(0);
                    break;

                  case 'i':     // right-hand version
                  case 'e':     // left-hand version
                  case KeyEvent.VK_UP:      // keyboard cursor
                    outputMessage = "Move forward";
                    resultValue = Integer.toString(gopigo.motion.forward(false));
                    break;

                  case 'j':     // right-hand version
                  case 's':     // left-hand version
                  case KeyEvent.VK_LEFT:      // keyboard cursor
                    outputMessage = "turn left";
                    resultValue = Integer.toString(gopigo.motion.left());
                    break;

                  case 'l':     // right-hand version
                  case 'f':     // left-hand version
                  case KeyEvent.VK_RIGHT:      // keyboard cursor
                    outputMessage = "turn right";
                    resultValue = Integer.toString(gopigo.motion.right());
                    break;

                  case 'k':     // right-hand version
                  case 'd':     // left-hand version
                  case KeyEvent.VK_DOWN:      // keyboard cursor
                    outputMessage = "back up";
                    resultValue = Integer.toString(gopigo.motion.backward(false));
                    break;

                  case ' ':     // either-hand version
                    outputMessage = "stop";
                    resultValue = Integer.toString(gopigo.motion.stop());
                    gopigo.ledLeft.off();
                    gopigo.ledRight.off();
                    break;

                  case 'h':     // right-hand version
                  case 'a':     // left-hand version
                    outputMessage = "rotate left";
                    resultValue = Integer.toString(gopigo.motion.leftWithRotation());
                    break;

                  case ';':     // right-hand version
                  case 'g':     // left-hand version
                    outputMessage = "rotate right";
                    resultValue = Integer.toString(gopigo.motion.rightWithRotation());
                    break;

                  case '8':     // right-hand version
                  case '4':     // left-hand version
                  case KeyEvent.VK_PAGE_UP:      // keyboard cursor
                    outputMessage = "speed up";
                    resultValue = Integer.toString(gopigo.motion.increaseSpeed());
                    break;

                  case 'q': // lowercase q
                    outputMessage = "left LED";
                    if (!ledLeft) {
                        ledLeft = true;
                        resultValue = Integer.toString(gopigo.ledLeft.on());
                    } else {
                        ledLeft = false;
                        resultValue = Integer.toString(gopigo.ledLeft.off());
                    }
                    break;
                  case '[':
                    outputMessage = "right LED";
                    if (!ledRight) {
                        ledRight = true;
                        resultValue = Integer.toString(gopigo.ledRight.on());
                    } else {
                        ledRight = false;
                        resultValue = Integer.toString(gopigo.ledRight.off());
                    }
                    break;

                  case ',':     // right-hand version
                  case 'x':     // left-hand version, lowercase x
                  case KeyEvent.VK_PAGE_DOWN:      // keyboard cursor
                    outputMessage = "slow down";
                    resultValue = Integer.toString(gopigo.motion.decreaseSpeed());
                    break;

                  default:      // cack-handed version :-)
                    outputMessage = "Unrecognized key" + c;
                    resultValue = Integer.toString(gopigo.motion.stop());
                    System.out.println("Unrecognized key" + c);
                    break;
                }
                //System.out.print(c);
                repaint();
            } catch (IOException ex) {
              System.out.println("IO error trying Key Press!");
              System.exit(1);
            }
        }
    }

    public static void main(String[] s) throws IOException, InterruptedException {
        JFrame f = new JFrame();
        f.getContentPane().add(new basic_robot_gui());
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.pack();
        f.setVisible(true);
    }
}
