package com.example.keepncook;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Locale;

public class ScanActivity extends AppCompatActivity
{
    SurfaceView mCameraView;
    TextView mTextView;
    CameraSource mCameraSource;

    public static final String SCAN_ACTIVITY = "SCAN_ACTIVITY";
    public static final int requestPermissionID = 101;

    String product_name;
    Date expiration_date;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        mCameraView = findViewById(R.id.surfaceView);
        mTextView = findViewById(R.id.text_view);

        startCameraSource();
    }

    public void setProductName(String productName)
    {
        this.product_name = productName;
    }

    public void ConfirmDateAndSaveProduct(View v)
    {
        try
        {
            this.expiration_date = new SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE).parse(this.mTextView.getText().toString());

            final EditText productNameEditText = new EditText(this);
            AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Produit")
                .setMessage(
                    "Date d'expiration : " + this.expiration_date.toString() +
                    "\nRenseignez maintenant le nom du produit"
                )
                .setView(productNameEditText)
                .setPositiveButton("Enregistrer", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setProductName(String.valueOf(productNameEditText.getText()));
                        // TODO : Implémenter l'enregistrement du produit sur Firebase, Afficher un message de succès et rediriger
                    }
                })
                .setNegativeButton("Annuler", null)
                .create();
            dialog.show();

            Log.i(SCAN_ACTIVITY, "Nom du produit : " + this.product_name + ". Date d'expiration : " + this.expiration_date);
        }
        catch (ParseException e)
        {
            Log.e(SCAN_ACTIVITY, "Parsing error " + e.getMessage());
            Toast.makeText(this, "Le texte scanné n'est pas une date valide", Toast.LENGTH_SHORT).show();
        }
    }

    private void startCameraSource()
    {
        //Create the TextRecognizer
        final TextRecognizer textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();

        if (!textRecognizer.isOperational())
        {
            Log.w(SCAN_ACTIVITY, "Les dépendances du détécteur ne sont pas encore chargés");
        }
        else
        {
            //Initialize camerasource to use high resolution and set Autofocus on.
            mCameraSource = new CameraSource.Builder(getApplicationContext(), textRecognizer)
                    .setFacing(CameraSource.CAMERA_FACING_BACK)
                    .setRequestedPreviewSize(1280, 1024)
                    .setAutoFocusEnabled(true)
                    .setRequestedFps(2.0f)
                    .build();

            /**
             * Add call back to SurfaceView and check if camera permission is granted.
             * If permission is granted we can start our cameraSource and pass it to surfaceView
             */
            mCameraView.getHolder().addCallback(new SurfaceHolder.Callback()
            {
                @Override
                public void surfaceCreated(SurfaceHolder holder)
                {
                    try
                    {
                        if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
                        {

                            ActivityCompat.requestPermissions(ScanActivity.this,
                                    new String[]{Manifest.permission.CAMERA},
                                    requestPermissionID);
                            return;
                        }
                        mCameraSource.start(mCameraView.getHolder());
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }

                @Override
                public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                }

                /**
                 * Release resources for cameraSource
                 */
                @Override
                public void surfaceDestroyed(SurfaceHolder holder) {
                    mCameraSource.stop();
                }
            });

            //Set the TextRecognizer's Processor.
            textRecognizer.setProcessor(new Detector.Processor<TextBlock>()
            {
                @Override
                public void release() {
                }

                /**
                 * Detect all the text from camera using TextBlock and the values into a stringBuilder
                 * which will then be set to the textView.
                 * */
                @Override
                public void receiveDetections(Detector.Detections<TextBlock> detections)
                {
                    final SparseArray<TextBlock> items = detections.getDetectedItems();
                    if (items.size() != 0 )
                    {
                        mTextView.post(new Runnable() {
                            @Override
                            public void run() {
                                StringBuilder stringBuilder = new StringBuilder();
                                for(int i=0;i<items.size();i++){
                                    TextBlock item = items.valueAt(i);
                                    stringBuilder.append(item.getValue());
                                    stringBuilder.append("\n");
                                }
                                mTextView.setText(stringBuilder.toString());
                            }
                        });
                    }
                }
            });
        }
    }
}
