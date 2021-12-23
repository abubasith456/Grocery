package com.trizions.ui.dashboard.tab_fragments.clients;

import static android.view.View.GONE;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.trizions.BaseActivity;
import com.trizions.R;
import com.trizions.utils.Utils;

import java.util.HashMap;

import butterknife.BindView;
import butterknife.OnClick;

public class EditClientsActivity extends BaseActivity {

    @BindView(R.id.imageViewEditUploadClientsPicture)
    ImageView imageViewEditUploadClientsPicture;
    @BindView(R.id.textViewSelectImage)
    TextView textViewSelectImage;
    @BindView(R.id.textViewRemovePicture)
    TextView textViewRemovePicture;
    @BindView(R.id.editTextEditClientName)
    EditText editTextEditClientName;
    @BindView(R.id.editTextEditClientRole)
    EditText editTextEditClientRole;
    @BindView(R.id.editTextEditClientBusiness)
    EditText editTextEditClientBusiness;
    @BindView(R.id.editTextClientEditMobileNumber)
    EditText editTextClientEditMobileNumber;
    @BindView(R.id.editTextEditClientEmail)
    EditText editTextEditClientEmail;
    @BindView(R.id.editTextEditClientAddress)
    EditText editTextEditClientAddress;
    @BindView(R.id.mTextViewErrorClientName)
    TextView mTextViewErrorClientName;
    @BindView(R.id.mTextViewErrorClientRole)
    TextView mTextViewErrorClientRole;
    @BindView(R.id.mTextViewErrorClientCompany)
    TextView mTextViewErrorClientCompany;
    @BindView(R.id.mTextViewErrorClientMobileNumber)
    TextView mTextViewErrorClientMobileNumber;
    @BindView(R.id.mTextViewErrorClientEmail)
    TextView mTextViewErrorClientEmail;
    @BindView(R.id.mTextViewErrorClientAddress)
    TextView mTextViewErrorClientAddress;
    @BindView(R.id.layoutEditClient)
    LinearLayout layoutEditClient;
    @BindView(R.id.progressBar)
    FrameLayout progressBar;

    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int STORAGE_REQUEST_CODE = 300;
    private static final int IMAGE_PICK_GALLERY_CODE = 400;
    private static final int IMAGE_PICK_CAMERA_CODE = 500;
    private String[] cameraPermissions;
    private String[] storagePermissions;
    private Uri imageUri;
    String filePathAndName;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    String userId, clientImage, clientName, clientRole, clientBusiness, clientMobileNumber,
            clientEmail, clientAddress, clientId;
    private StorageReference filepath;
    Uri downloadImageUri;
    String timeStamp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_clients);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        userId = firebaseAuth.getCurrentUser().getUid();
        timeStamp = "" + System.currentTimeMillis();
        clientId = getIntent().getStringExtra("clientId");
        clientImage = getIntent().getStringExtra("clientImage");
        clientName = getIntent().getStringExtra("clientName");
        clientRole = getIntent().getStringExtra("clientRole");
        clientBusiness = getIntent().getStringExtra("clientBusiness");
        clientMobileNumber = getIntent().getStringExtra("clientMobileNumber");
        clientEmail = getIntent().getStringExtra("clientEmail");
        clientAddress = getIntent().getStringExtra("clientAddress");
        cameraPermissions = new String[]{Manifest.permission.CAMERA};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        setInfo();
    }

    private void setInfo() {
        try {
            editTextEditClientName.setText(clientName);
            editTextEditClientRole.setText(clientRole);
            editTextEditClientBusiness.setText(clientBusiness);
            editTextClientEditMobileNumber.setText(clientMobileNumber);
            editTextEditClientEmail.setText(clientEmail);
            editTextEditClientAddress.setText(clientAddress);
            if (clientImage == null) {
                imageViewEditUploadClientsPicture.setImageResource(R.drawable.tab_icon_about_us);
            }
            try {
                Picasso.get().load(clientImage).into(imageViewEditUploadClientsPicture);
                textViewSelectImage.setVisibility(GONE);
            } catch (Exception e) {
                textViewSelectImage.setVisibility(View.VISIBLE);
            }

        } catch (Exception exception) {
            Log.e("Error ==> ", "" + exception);
        }
    }

    @OnClick(R.id.buttonBackArrow)
    void backButtonClick() {
        try {
            finish();
        } catch (Exception exception) {
            Log.e("Error ==> ", "" + exception);
        }
    }

    @OnClick(R.id.imageViewEditUploadClientsPicture)
    void onImageViewUploadProductPictureClick() {
        try {
            showImagePickerDialog();
        } catch (Exception exception) {
            Log.e("Error ==> ", "" + exception);
        }
    }

    @OnClick(R.id.textViewRemovePicture)
    void onTextViewRemovePictureClick() {
        try {
            imageViewEditUploadClientsPicture.setImageDrawable(null);
        } catch (Exception exception) {
            Log.e("Error ==> ", "" + exception);
        }
    }

    @OnClick(R.id.layoutEditClient)
    void onLayoutAddClick() {
        try {
            if (validate(editTextEditClientName.getText().toString(), editTextEditClientRole.getText().toString(), editTextEditClientBusiness.getText().toString(),
                    editTextClientEditMobileNumber.getText().toString(), editTextEditClientEmail.getText().toString(), editTextEditClientAddress.getText().toString())) {
                showProgress();
                Utils.hideSoftKeyboard(this);
                if (imageUri == null) {
                    //upload without image
                    uploadDataWithoutImage();
                } else {
                    //with Image
                    //store image to firebase storage with name and path
                    filePathAndName = "Clients/" + "" + timeStamp;
                    filepath = FirebaseStorage.getInstance().getReference(filePathAndName).child(imageUri.getEncodedPath());
                    getPathFromFirebaseStorage();
                }
            }
        } catch (Exception exception) {
            Log.e("Error ==> ", "" + exception);
        }
    }

    private void showImagePickerDialog() {
        String[] options = {"Camera", "Gallery"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick Image").setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    if (checkCameraPermission()) {
                        pickFromCamera();
                    } else {
                        requestCameraPermission();
                    }
                } else {
                    if (checkStoragePermission()) {
                        pickFromGallery();
                    } else {
                        requestStoragePermission();
                    }
                }
            }
        }).show();
    }

    private void getPathFromFirebaseStorage() {
        try {
            filepath.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                    while (!uriTask.isSuccessful()) ;
                    downloadImageUri = uriTask.getResult();
                    if (uriTask.isSuccessful()) {
                        //url uri received,upload to DB
                        uploadDataWithImage();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    hideProgress();
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception exception) {
            Log.e("Error Add item==> ", "" + exception);
        }
    }

    private void uploadDataWithImage() {
        try {
            HashMap<String, Object> addFieldInfo = new HashMap<>();
            addFieldInfo.put("clientImage", "" + downloadImageUri);
            addFieldInfo.put("clientName", "" + editTextEditClientName.getText().toString());
            addFieldInfo.put("clientRole", "" + editTextEditClientRole.getText().toString());
            addFieldInfo.put("clientBusinessName", "" + editTextEditClientBusiness.getText().toString());
            addFieldInfo.put("clientMobileNumber", "" + editTextClientEditMobileNumber.getText().toString());
            addFieldInfo.put("clientEmail", "" + editTextEditClientEmail.getText().toString());
            addFieldInfo.put("clientAddress", "" + editTextEditClientAddress.getText().toString());
            addFieldInfo.put("userId", "" + userId);
            DocumentReference databaseReference = firebaseFirestore.collection("Clients").document(clientId);
            databaseReference.update(addFieldInfo).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(@NonNull Void unused) {
                    hideProgress();
                    Toast.makeText(getApplicationContext(), "Edited", Toast.LENGTH_SHORT).show();
                    if (clientImage != downloadImageUri.toString()) {
                        deleteFirebaseStorageImage();
                    }
                    finish();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    hideProgress();
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception exception) {
            Log.e("Error Add item==> ", "" + exception);
        }

    }

    private void uploadDataWithoutImage() {
        try {
            if (imageViewEditUploadClientsPicture.getDrawable() == null) {
                deleteFirebaseStorageImage();
                clientImage = "";
            }
            HashMap<String, Object> addFieldInfo = new HashMap<>();
            addFieldInfo.put("clientImage", "" + clientImage);//No image
            addFieldInfo.put("clientName", "" + editTextEditClientName.getText().toString());
            addFieldInfo.put("clientRole", "" + editTextEditClientRole.getText().toString());
            addFieldInfo.put("clientBusinessName", "" + editTextEditClientBusiness.getText().toString());
            addFieldInfo.put("clientMobileNumber", "" + editTextClientEditMobileNumber.getText().toString());
            addFieldInfo.put("clientEmail", "" + editTextEditClientEmail.getText().toString());
            addFieldInfo.put("clientAddress", "" + editTextEditClientAddress.getText().toString());
            addFieldInfo.put("userId", "" + userId);
            DocumentReference databaseReference = firebaseFirestore.collection("Clients").document(clientId);
            databaseReference.update(addFieldInfo).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(@NonNull Void unused) {
                    hideProgress();
                    Toast.makeText(getApplicationContext(), "Edited", Toast.LENGTH_SHORT).show();

                    finish();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    hideProgress();
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception exception) {
            Log.e("Error Add item==> ", "" + exception);
        }
    }

    void deleteFirebaseStorageImage() {
        try {
            FirebaseStorage mFirebaseStorage = FirebaseStorage.getInstance();
            StorageReference photoRef = mFirebaseStorage.getReferenceFromUrl(clientImage);
            photoRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(getApplicationContext(), "Photo deleted", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Toast.makeText(getApplicationContext(), exception.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception exception) {
            Log.e("Error ==> ", "" + exception);
        }
    }

    private void pickFromCamera() {
        //using media to pic high quality image
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, "Temp_image_title");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Temp_image_description");
        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
        Intent pickCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        pickCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(pickCameraIntent, IMAGE_PICK_CAMERA_CODE);
    }

    private void pickFromGallery() {
        Intent picGalleryIntent = new Intent(Intent.ACTION_PICK);
        picGalleryIntent.setType("image/*");
        startActivityForResult(picGalleryIntent, IMAGE_PICK_GALLERY_CODE);
    }

    private boolean checkCameraPermission() {
        boolean resultCamera = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
                (PackageManager.PERMISSION_GRANTED);
        return resultCamera;
    }

    private boolean checkStoragePermission() {
        boolean resultStorage = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                (PackageManager.PERMISSION_GRANTED);
        return resultStorage;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, cameraPermissions, CAMERA_REQUEST_CODE);
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this, storagePermissions, STORAGE_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {
            case CAMERA_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted) {
                        pickFromCamera();
                    } else {
                        Toast.makeText(getApplicationContext(), "Camera permission required..", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
            case STORAGE_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (storageAccepted) {
                        pickFromGallery();
                    } else {
                        Toast.makeText(getApplicationContext(), "Storage permission required..", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    //Result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_PICK_GALLERY_CODE && resultCode == RESULT_OK) {
            imageUri = data.getData();
            imageViewEditUploadClientsPicture.setImageURI(imageUri);
            if (imageUri != null) {
                textViewSelectImage.setVisibility(GONE);
            }
        } else if (requestCode == IMAGE_PICK_CAMERA_CODE && resultCode == RESULT_OK) {
            imageViewEditUploadClientsPicture.setImageURI(imageUri);
            if (imageUri != null) {
                textViewSelectImage.setVisibility(GONE);
            }
        }
    }

    private boolean validate(String clientName, String clientRole, String clientBusinessName, String clientMobileNumber, String clientEmail, String clientAddress) {
        boolean valid = true;
        try {
            if (clientName.isEmpty()) {
                editTextEditClientName.setBackground(getResources().getDrawable(R.drawable.background_rounded_edit_text_error));
                mTextViewErrorClientName.setVisibility(View.VISIBLE);
                mTextViewErrorClientName.setText(getResources().getString(R.string.error_client_name));
                valid = false;
            } else {
                editTextEditClientAddress.setBackground(getResources().getDrawable(R.drawable.background_rounded_edit_text_gray));
                mTextViewErrorClientName.setVisibility(GONE);
            }
            if (clientRole.isEmpty()) {
                editTextEditClientRole.setBackground(getResources().getDrawable(R.drawable.background_rounded_edit_text_error));
                mTextViewErrorClientRole.setVisibility(View.VISIBLE);
                mTextViewErrorClientRole.setText(getResources().getString(R.string.error_client_role));
                valid = false;
            } else {
                editTextEditClientRole.setBackground(getResources().getDrawable(R.drawable.background_rounded_edit_text_gray));
                mTextViewErrorClientRole.setVisibility(GONE);
            }
            if (clientBusinessName.isEmpty()) {
                editTextEditClientBusiness.setBackground(getResources().getDrawable(R.drawable.background_rounded_edit_text_error));
                mTextViewErrorClientCompany.setVisibility(View.VISIBLE);
                mTextViewErrorClientCompany.setText(getResources().getString(R.string.error_client_bussiness));
                valid = false;
            } else {
                editTextEditClientBusiness.setBackground(getResources().getDrawable(R.drawable.background_rounded_edit_text_gray));
                mTextViewErrorClientCompany.setVisibility(GONE);
            }
            if (clientMobileNumber.isEmpty()) {
                editTextClientEditMobileNumber.setBackground(getResources().getDrawable(R.drawable.background_rounded_edit_text_error));
                mTextViewErrorClientMobileNumber.setVisibility(View.VISIBLE);
                mTextViewErrorClientMobileNumber.setText(getResources().getString(R.string.error_client_mobile));
                valid = false;
            } else {
                editTextClientEditMobileNumber.setBackground(getResources().getDrawable(R.drawable.background_rounded_edit_text_gray));
                mTextViewErrorClientMobileNumber.setVisibility(GONE);
            }
            if (clientEmail.isEmpty()) {
                editTextEditClientEmail.setBackground(getResources().getDrawable(R.drawable.background_rounded_edit_text_error));
                mTextViewErrorClientEmail.setVisibility(View.VISIBLE);
                mTextViewErrorClientEmail.setText(getResources().getString(R.string.error_client_email));
                valid = false;
            } else {
                editTextEditClientEmail.setBackground(getResources().getDrawable(R.drawable.background_rounded_edit_text_gray));
                mTextViewErrorClientEmail.setVisibility(GONE);
            }
            if (clientAddress.isEmpty()) {
                editTextEditClientAddress.setBackground(getResources().getDrawable(R.drawable.background_rounded_edit_text_error));
                mTextViewErrorClientAddress.setVisibility(View.VISIBLE);
                mTextViewErrorClientAddress.setText(getResources().getString(R.string.error_client_address));
                valid = false;
            } else {
                editTextEditClientAddress.setBackground(getResources().getDrawable(R.drawable.background_rounded_edit_text_gray));
                mTextViewErrorClientAddress.setVisibility(GONE);
            }
        } catch (Exception exception) {
            Log.e("Error ==> ", "" + exception);
        }
        return valid;
    }

    public void showProgress() {
        try {
            progressBar.setVisibility(View.VISIBLE);
        } catch (Exception exception) {
            Log.e("Error ==> ", "" + exception);
        }
    }

    public void hideProgress() {
        try {
            progressBar.setVisibility(View.GONE);
        } catch (Exception exception) {
            Log.e("Error ==> ", "" + exception);
        }
    }
}