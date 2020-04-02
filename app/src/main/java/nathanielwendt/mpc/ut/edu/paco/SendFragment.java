package nathanielwendt.mpc.ut.edu.paco;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.karumi.dexter.listener.single.PermissionListener;

import org.eclipse.paho.android.service.MqttAndroidClient;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import nathanielwendt.mpc.ut.edu.paco.fire_MQTT.sendData;


public class SendFragment extends Fragment {

    private EditText in_message;
    private EditText receiverToken;
    private EditText Title;
    private Button btn_send;
    private SwitchCompat mSwitchCompat;

    private ImageView poster;
    protected Button cameraButton;
    protected Button storageButton;
    protected String posterPath;
    protected boolean storagePermissions = false;
    private CreatePlaceFragment.CreatePlaceFragmentDoneListener mListener;
    private Uri imageUri;
    private String strImage;
    private Bitmap mBitmap;
    private static final int SELECT_PHOTO = 101;
    private static final int ACTION_TAKE_PICTURE = 100;

    private String SenderToken;
    private String ReceiverToken;
    private String title;
    private String message;
    private String data;
    int stage;

    MqttAndroidClient client;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_send, container, false);

        Dexter.checkPermissions(storagePermissionsListener, Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);

        //capture ui elements
        in_message = (EditText) root.findViewById(R.id.message);
        Title = (EditText) root.findViewById(R.id.title);
        receiverToken = (EditText) root.findViewById(R.id.Receiver);
        btn_send = (Button) root.findViewById(R.id.btn_send);
        poster = (ImageView) root.findViewById(R.id.imageView);
        this.cameraButton = (Button) root.findViewById(R.id.pickFromCamera);
        this.storageButton = (Button) root.findViewById(R.id.pickFromGallery);

        //setup ui elements
        this.cameraButton.setOnClickListener(cameraListener);
        this.storageButton.setOnClickListener(storageListener);

        client = ((MainActivity)getActivity()).getClient();

        //Button Switch
        mSwitchCompat = root.findViewById(R.id.s_switch);
        mSwitchCompat.setChecked(false);

        //place SEND
        Bundle bundle=getArguments();
        if(bundle != null){
            if(getArguments().getString("message")!=null){in_message.setText(getArguments().getString("message"));}
            if(getArguments().getString("title")!=null){Title.setText(getArguments().getString("title"));}
            if(getArguments().getString("image")!=null) {
                String uri = getArguments().getString("image");

                File pictureFile = new File(uri.toString());
                String file_url = "file://" + uri;
                if (pictureFile.exists()) {
                    try (InputStream is = new URL(file_url).openStream()) {
                        mBitmap = BitmapFactory.decodeStream(is);
                        poster.setImageBitmap(mBitmap);
                    } catch (Exception e) {
                        Log.d("Error", e.toString());
                    }
                }
            }
        }

        //default for firebase
        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bitmap b = Bitmap.createScaledBitmap(mBitmap,(int)(mBitmap.getWidth()*0.03), (int)(mBitmap.getHeight()*0.03), true);
                strImage = getStringImage(b);
                ((MainActivity)getActivity()).mPaco.SEND(sendData());//

            }
        });

        mSwitchCompat.setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            //MQTT
                            btn_send.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    strImage = getStringImage(mBitmap);
                                    ((MainActivity)getActivity()).mPaco.sendMQTT(sendData());//

                                }
                            });
                        } else {
                            //Firebase
                            btn_send.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Bitmap b = Bitmap.createScaledBitmap(mBitmap,(int)(mBitmap.getWidth()*0.03), (int)(mBitmap.getHeight()*0.03), true);
                                    strImage = getStringImage(b);
                                    ((MainActivity)getActivity()).mPaco.SEND(sendData());//

                                }
                            });
                        }
                    }
                }
        );

        return root;

    }

    private MultiplePermissionsListener storagePermissionsListener = new MultiplePermissionsListener() {
        @Override
        public void onPermissionsChecked(MultiplePermissionsReport report) {
            storagePermissions = true;
        }

        @Override
        public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
            token.continuePermissionRequest();
        }
    };

    Button.OnClickListener cameraListener = new Button.OnClickListener() {
        public void onClick(View v) {
            Dexter.checkPermission(cameraPermissionListener, Manifest.permission.CAMERA);
        }
    };

    private PermissionListener cameraPermissionListener = new PermissionListener() {
        @Override
        public void onPermissionGranted(PermissionGrantedResponse response) {
            try {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                ContentValues values = new ContentValues();//

                Uri photoURI = getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                imageUri = photoURI;
                if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivityForResult(intent, ACTION_TAKE_PICTURE);
                }
            } catch (Exception ex) {
                ex.printStackTrace();

            }
        }

        @Override
        public void onPermissionDenied(PermissionDeniedResponse response) {
            mListener.showSnackBar("Camera permissions denied, cannot start camera");
        }

        @Override
        public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
            token.continuePermissionRequest();
        }
    };

    Button.OnClickListener storageListener = new Button.OnClickListener() {
        public void onClick(View v) {
            if(storagePermissions){
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                //We pass an extra array with the accepted mime types. This will ensure only components with these MIME types as targeted.
                String[] mimeTypes = {"image/jpeg", "image/png"};
                photoPickerIntent.putExtra(Intent.EXTRA_MIME_TYPES,mimeTypes);
                startActivityForResult(photoPickerIntent, SELECT_PHOTO);
            }

        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        MainActivity activity = (MainActivity) getActivity();
        switch (requestCode){
            case SELECT_PHOTO:
                Uri selecImage = data.getData();
                if (resultCode == Activity.RESULT_OK && data!= null && data.getData() != null) {
                    poster.setImageURI(selecImage);
                    //posterPath = getRealPathFromURI(activity, selecImage);
                    //posterPath = getRealPathFromURI(selecImage);
                    mBitmap = getBitmapFromCameraData(selecImage, activity);
                    strImage = getStringImage(mBitmap);
                    break;
                }

            case ACTION_TAKE_PICTURE:
                if (resultCode == Activity.RESULT_OK) {
                    poster.setImageURI(imageUri);
                    //posterPath = getRealPathFromURI(imageUri);
                    //Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                    mBitmap = getBitmapFromCameraData(imageUri, activity);
                    strImage = getStringImage(mBitmap);
                    break;
                }
        }
    }

    public static Bitmap getBitmapFromCameraData(Uri ImageUri, Context context){
        String[] filePathColumn = { MediaStore.Images.Media.DATA };
        Cursor cursor = context.getContentResolver().query(ImageUri, filePathColumn, null, null, null);
        cursor.moveToFirst();
        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String picturePath = cursor.getString(columnIndex);
        cursor.close();
        return BitmapFactory.decodeFile(picturePath);
    }

    public String getStringImage(Bitmap bmp){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;
    }

    private String sendData(){
        SenderToken =((MainActivity)getActivity()).getTheCurrentClient();
        ReceiverToken = receiverToken.getText().toString();
        title = Title.getText().toString();
        message = in_message.getText().toString();
        stage = 0;

        //share places
        sendData data = new sendData();
        data.setStage(0);
        data.setDataOwnderToken(ReceiverToken);
        data.setRequesterToken(SenderToken);
        data.setTitle(title);
        data.setMessage(message);
        data.setUri(strImage);
        return data.getSendData();

    }
}