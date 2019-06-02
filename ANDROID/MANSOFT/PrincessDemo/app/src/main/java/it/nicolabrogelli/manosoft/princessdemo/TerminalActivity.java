package it.nicolabrogelli.manosoft.princessdemo;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.ImageLoader;
import it.nicolabrogelli.bluetoothlib.BluetoothDeviceListDialog;
import it.nicolabrogelli.bluetoothlib.BluetoothSerial;
import it.nicolabrogelli.bluetoothlib.BluetoothSerialListener;

import it.nicolabrogelli.manosoft.princessdemo.util.CHexConver;
import it.nicolabrogelli.manosoft.princessdemo.util.MySingleton;

/**
 * This is an example Bluetooth terminal application built using the Blue2Serial library.
 *
 * @author Macro Yau
 */
public class TerminalActivity extends AppCompatActivity
        implements BluetoothSerialListener, BluetoothDeviceListDialog.OnDeviceSelectedListener {

    private static final int REQUEST_ENABLE_BLUETOOTH = 1;


    de.hdodenhof.circleimageview.CircleImageView imageGame;

    private BluetoothSerial bluetoothSerial;
    private ImageLoader mImageLoader;


    private ScrollView svTerminal;
    private TextView tvTerminal;
    private EditText etSend;

    private MenuItem actionConnect, actionDisconnect, actionTest;

    private boolean crlf = false;

    private String absolutePath ="\\";
    private String messagePrincess = "";
    private String sendCommand = "";
    private String commandArray[];
    private String cmdTemp = "";

    int lenCommandArray = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terminal);

        // Find UI views and set listeners
        svTerminal = (ScrollView) findViewById(R.id.terminal);
        tvTerminal = (TextView) findViewById(R.id.tv_terminal);
        etSend = (EditText) findViewById(R.id.et_send);
        etSend.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    String send = commandPrincess(etSend.getText().toString().trim());
                    if (send.length() > 0) {
                        bluetoothSerial.write(CHexConver.hexStr2Bytes(send.concat("\r\n")), crlf);
                        etSend.setText("");
                    }
                }
                return false;
            }
        });

        // Create a new instance of BluetoothSerial
        bluetoothSerial = new BluetoothSerial(this, this);

    }

    @Override
    protected void onStart() {
        super.onStart();

        // Check Bluetooth availability on the device and set up the Bluetooth adapter
        bluetoothSerial.setup();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Open a Bluetooth serial port and get ready to establish a connection
        if (bluetoothSerial.checkBluetooth() && bluetoothSerial.isBluetoothEnabled()) {
            if (!bluetoothSerial.isConnected()) {
                bluetoothSerial.start();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Disconnect from the remote device and close the serial port
        bluetoothSerial.stop();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_terminal, menu);

        actionConnect = menu.findItem(R.id.action_connect);
        actionDisconnect = menu.findItem(R.id.action_disconnect);


        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_connect) {
            showDeviceListDialog();
            return true;
        } else if (id == R.id.action_disconnect) {
            bluetoothSerial.stop();
            return true;
        } else if (id == R.id.action_crlf) {
            crlf = !item.isChecked();
            item.setChecked(crlf);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void invalidateOptionsMenu() {
        if (bluetoothSerial == null)
            return;

        // Show or hide the "Connect" and "Disconnect" buttons on the app bar
        if (bluetoothSerial.isConnected()) {
            if (actionConnect != null) {
                actionConnect.setVisible(false);

                /* Reset della scheda al momento della connessione
                new Thread(new Runnable() {
                    public void run() {
                        Toast.makeText(TerminalActivity.this, "Inizzializzazione scheda", Toast.LENGTH_LONG).show();
                        SystemClock.sleep(300);
                        cmdTemp = "1B000D0A"; // + CHexConver.str2HexStr(((ElementoLista) listItem).getTitolo().substring(4,((ElementoLista) listItem).getTitolo().length()).trim()) + "0D0A";
                        sendCommand = cmdTemp.trim();
                        bluetoothSerial.write(CHexConver.hexStr2Bytes(sendCommand.concat("\r\n")), crlf);
                        messagePrincess = "";
                    }
                }).start();
                */
            }
            if (actionDisconnect != null)
                actionDisconnect.setVisible(true);
        } else {
            if (actionConnect != null)
                actionConnect.setVisible(true);
            if (actionDisconnect != null)
                actionDisconnect.setVisible(false);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_ENABLE_BLUETOOTH:
                // Set up Bluetooth serial port when Bluetooth adapter is turned on
                if (resultCode == Activity.RESULT_OK) {
                    bluetoothSerial.setup();
                }
                break;
        }
    }

    private void updateBluetoothState() {
        // Get the current Bluetooth state
        final int state;
        if (bluetoothSerial != null)
            state = bluetoothSerial.getState();
        else
            state = BluetoothSerial.STATE_DISCONNECTED;

        // Display the current state on the app bar as the subtitle
        String subtitle;
        switch (state) {
            case BluetoothSerial.STATE_CONNECTING:
                subtitle = getString(R.string.status_connecting);
                break;
            case BluetoothSerial.STATE_CONNECTED:
                subtitle = getString(R.string.status_connected, bluetoothSerial.getConnectedDeviceName());
                break;
            default:
                subtitle = getString(R.string.status_disconnected);
                break;
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setSubtitle(subtitle);
        }
    }

    private void showDeviceListDialog() {
        // Display dialog for selecting a remote Bluetooth device
        BluetoothDeviceListDialog dialog = new BluetoothDeviceListDialog(this);
        dialog.setOnDeviceSelectedListener(this);
        dialog.setTitle(R.string.paired_devices);
        dialog.setDevices(bluetoothSerial.getPairedDevices());
        dialog.showAddress(true);
        dialog.show();
    }

    /**
     *
     * @param command
     * @param nameGame
     */
    private void showDialogGame(final String command, final String nameGame) {

        AlertDialog.Builder builder = new AlertDialog.Builder(TerminalActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialod_detailgame,null);
        builder.setView(dialogView);

        ImageButton btnPlay = (ImageButton) dialogView.findViewById(R.id.btnPlay);
        ImageButton btnPause = (ImageButton) dialogView.findViewById(R.id.btnPause);
        ImageButton btnStop = (ImageButton) dialogView.findViewById(R.id.btnStop);
        ImageButton btnEject = (ImageButton) dialogView.findViewById(R.id.btnEject);
        ImageButton btnFavorite = (ImageButton) dialogView.findViewById(R.id.btnFavorite);
        TextView tvNameGame = (TextView) dialogView.findViewById(R.id.tvNameGame);
        imageGame  = (de.hdodenhof.circleimageview.CircleImageView) dialogView.findViewById(R.id.imageGame);

        final AlertDialog dialog = builder.create();

        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {

            }
        });

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                if (command.length() > 0) {
                    Toast.makeText(TerminalActivity.this, "Play", Toast.LENGTH_LONG).show();
                    bluetoothSerial.write(CHexConver.hexStr2Bytes(command.concat("\r\n")), crlf);
                    etSend.setText("");
                }

            }
        });

        btnPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(TerminalActivity.this, "Pause", Toast.LENGTH_LONG).show();
            }
        });

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Toast.makeText(TerminalActivity.this, "Stop", Toast.LENGTH_LONG).show();

            }
        });

        btnEject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
                etSend.setText("");
                Toast.makeText(TerminalActivity.this, "Eject", Toast.LENGTH_LONG).show();
            }
        });

        btnFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Toast.makeText(TerminalActivity.this, "Aggiunto hai Favoriti", Toast.LENGTH_LONG).show();

            }
        });


        mImageLoader    = MySingleton.getInstance(this).getImageLoader();
            mImageLoader.get("http://www.bertiinformatica.it/imggame/" + nameGame.substring(0,nameGame.length()-3) + "png",
                    com.android.volley.toolbox.ImageLoader.getImageListener(imageGame,
                            R.mipmap.empty_photo, R.mipmap.empty_photo));


        /*Display the custom alert dialog on interface
        Picasso.with(dialogView.getContext()).load("http://www.bertiinformatica.it/imggame/" + nameGame.substring(1,nameGame.length()-3) + "png").into(imageGame, new com.squareup.picasso.Callback() {
            @Override
            public void onSuccess() {
                Toast.makeText(TerminalActivity.this, "Immagine recuperata", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError() {
                Toast.makeText(TerminalActivity.this, "Errore recupero immagine", Toast.LENGTH_LONG).show();
                Picasso.with(TerminalActivity.this).load(R.drawable.afterburner).into(imageGame);
            }
        });*/

        tvNameGame.setText(nameGame);

        dialog.setCancelable(false);
        dialog.show();

    }

    /* Implementation of BluetoothSerialListener */

    @Override
    public void onBluetoothNotSupported() {
        new AlertDialog.Builder(this)
                .setMessage(R.string.no_bluetooth)
                .setPositiveButton(R.string.action_quit, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setCancelable(false)
                .show();
    }

    @Override
    public void onBluetoothDisabled() {
        Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBluetooth, REQUEST_ENABLE_BLUETOOTH);
    }

    @Override
    public void onBluetoothDeviceDisconnected() {
        invalidateOptionsMenu();
        updateBluetoothState();
    }

    @Override
    public void onConnectingBluetoothDevice() {
        updateBluetoothState();
    }

    @Override
    public void onBluetoothDeviceConnected(String name, String address) {
        invalidateOptionsMenu();
        updateBluetoothState();
    }

    @Override
    public void onBluetoothSerialRead(String message) {
        // Print the incoming message on the terminal screen

        /* Nicola
        tvTerminal.append(getString(R.string.terminal_message_template,
                bluetoothSerial.getConnectedDeviceName(),
                message));
        */

        byte[] commsndExecuteCorrectly = {27,01,00,13,10};        //{27,1,0,13,10}
        byte[] closeResponseList = {27,03,13,10};
        byte[] startResponseOpen = {27,8};                  //{27,1,8,13,10}
        byte[] cantOpenFile      = {27,1};                  //{27,1,1,13,10}
        byte[] notEnoughMemory   = {27,2};                  //{27,1,2,13,10}
        byte[] cantOpenDirectory = {27,3};                  //{27,1,3,13,10}
        byte[] cantReachOffset   = {27,4};                  //{27,1,4,13,10}
        byte[] cantChangeDirectory = {27,5};                //{27,1,5,13,10}
        byte[] fileNotFound      = {27,6};                  //{27,1,6,13,10}

        byte[] startFileFolder   = {27,3};
        byte[] endFileFoledr     = {13,10};

        messagePrincess +=message;

        // Tentativo di elaborare il comando list
        if(indexOf(messagePrincess.getBytes(), commsndExecuteCorrectly) == 0) {
            if(indexOf(messagePrincess.getBytes(),closeResponseList) > -1) {
                int lenMessagePrincess = messagePrincess.length() - commsndExecuteCorrectly.length - closeResponseList.length ;
                byte[] buffer = new byte[lenMessagePrincess];
                System.arraycopy(messagePrincess.substring(5,messagePrincess.length() - 4).getBytes(), 0, buffer, 0,lenMessagePrincess);

                String tmp = new String(buffer);
                while (true) {
                    if (indexOf(tmp.getBytes(), startFileFolder) == 0) {
                        if (indexOf(tmp.getBytes(), endFileFoledr) > -1) {
                            displayMessage(extractNameFileFolder(tmp.substring(2,indexOf(tmp.getBytes(), endFileFoledr))).concat("\r\n"));
                            tmp = tmp.substring(indexOf(tmp.getBytes(), endFileFoledr) + 2);
                        }
                    } else {
                        break;
                    }
                }
            }
        } // Si elabora il comando open, root e up
        else if (indexOf(messagePrincess.getBytes(), startResponseOpen) == 0) {
            if(indexOf(messagePrincess.getBytes(),commsndExecuteCorrectly) > -1) {
                int lenMessagePrincess = messagePrincess.length() - commsndExecuteCorrectly.length - closeResponseList.length ;
                byte[] buffer = new byte[lenMessagePrincess];
                System.arraycopy(messagePrincess.substring(2,messagePrincess.length() - 4).getBytes(), 0, buffer, 0,lenMessagePrincess - 1);
                displayMessage(messagePrincess);
            }
        }
        else if (indexOf(messagePrincess.getBytes(), fileNotFound) == 0) {
            tvTerminal.append("File not found!".concat("\r\n"));
            messagePrincess = "";
        }

    }

    @Override
    public void onBluetoothSerialWrite(String message) {
        // Print the outgoing message on the terminal screen
        /* Nicola
        tvTerminal.append(getString(R.string.terminal_message_template,
                bluetoothSerial.getLocalAdapterName(),
                message));
        svTerminal.post(scrollTerminalToBottom);
        */
    }

    /* Implementation of BluetoothDeviceListDialog.OnDeviceSelectedListener */

    @Override
    public void onBluetoothDeviceSelected(BluetoothDevice device) {
        // Connect to the selected remote Bluetooth device
        bluetoothSerial.connect(device);
    }

    /* End of the implementation of listeners */

    private final Runnable scrollTerminalToBottom = new Runnable() {
        @Override
        public void run() {
            // Scroll the terminal screen to the bottom
            svTerminal.fullScroll(ScrollView.FOCUS_DOWN);
        }
    };


    /**
     *
     * @param command
     * @return
     */
    public String commandPrincess(String command) {

        String nameGame = "";

        sendCommand = "";
        cmdTemp = "";
        commandArray = command.toString().split(" ");
        lenCommandArray = commandArray.length;
        messagePrincess = "";


        if (commandArray[0].equals("open")) {
            // mount folder
            for (int cnt = 1; cnt <= lenCommandArray - 1; cnt++) {
                cmdTemp += " " +commandArray[cnt];
            }
            cmdTemp = "1B043A" + CHexConver.str2HexStr(cmdTemp.trim()) + "0D0A";
            sendCommand = cmdTemp.trim();
            tvTerminal.setText("");
        } else if(commandArray[0].equals("eject")) {
            sendCommand = "1B043A5F0D0A";
        } else if (commandArray[0].equals("list")) {
            // explore folder
            sendCommand = "1B030D0A";
        } else if(commandArray[0].equals("tap") || commandArray[0].equals("play")) {
            // tap or play
            for (int cnt = 1; cnt <= lenCommandArray - 1; cnt++) {
                cmdTemp += " " + commandArray[cnt];
            }
            cmdTemp = "1B02" + "30303030303000" + CHexConver.str2HexStr(cmdTemp.trim()) + "0D0A";
            sendCommand = cmdTemp.trim();
            tvTerminal.setText("");

        } else if(commandArray[0].equals("root")) {
            // root
            sendCommand = "1B042F2F0D0A";
            tvTerminal.setText("");
        } else if(commandArray[0].equals("up")) {
            // levelup
            sendCommand = "1B043A5F0D0A";
            tvTerminal.setText("");
        } else if(commandArray[0].equals("reset")) {
            // reset princess
            sendCommand = "1B00" + "0D0A";
        } else if(commandArray[0].equals("clr")) {
            // cleat screen
            tvTerminal.setText("");
            etSend.setText("");
        }
        else {
            Toast.makeText(this, "Comando non implementato", Toast.LENGTH_LONG).show();
            sendCommand = "";
            etSend.setText("");
        }

        return  sendCommand;
    }

    /**
     *
     * @param outerArray
     * @param smallerArray
     * @return
     */
    public int indexOf(byte[] outerArray, byte[] smallerArray) {
        for(int i = 0; i < outerArray.length - smallerArray.length + 1; ++i) {
            boolean found = true;
            for(int j = 0; j < smallerArray.length; ++j) {
                if (outerArray[i+j] != smallerArray[j]) {
                    found = false;
                    break;
                }
            }
            if (found) return i;
        }
        return -1;
    }


    /**
     *
     * @param messagePrincess
     */
    public void displayMessage(String messagePrincess) {
        tvTerminal.append(messagePrincess);
        svTerminal.post(scrollTerminalToBottom);
        messagePrincess = "";
    }

    public String extractNameFileFolder(String message) {

        String returnMessage = "";
        String[] msg = message.split(",");
        if(msg.length == 2) {
            returnMessage = "(" + msg[0] + ") " + msg[1];
        } else if(msg.length == 3) {
            returnMessage = "(" + msg[0] + ") " +msg[2];
        } else if(msg.length == 4) {
            returnMessage = "(" + msg[0] + ") " + msg[3];
        }
        return returnMessage;
    }




}
