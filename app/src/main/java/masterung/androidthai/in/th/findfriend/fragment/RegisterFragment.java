package masterung.androidthai.in.th.findfriend.fragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import masterung.androidthai.in.th.findfriend.MainActivity;
import masterung.androidthai.in.th.findfriend.R;
import masterung.androidthai.in.th.findfriend.utility.MyAlert;
import masterung.androidthai.in.th.findfriend.utility.UserModel;

public class RegisterFragment extends Fragment {

    private String nameString, emailString, passwordString, pathAvataString, uidUserString;
    private Uri uri;
    private ImageView imageView;
    private boolean chooseBool = true;


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

//        Create Toolbar
        createToolbar();

//        Avata Controller
        avataController();

    }   // Main Method

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == getActivity().RESULT_OK) {

            uri = data.getData();
            chooseBool = false;

            try {

                Bitmap bitmap = BitmapFactory
                        .decodeStream(getActivity()
                                .getContentResolver()
                                .openInputStream(uri));
                imageView.setImageBitmap(bitmap);

            } catch (Exception e) {
                e.printStackTrace();
            }



        }   // if
    }   // onActivityResult

    private void avataController() {

        imageView = getView().findViewById(R.id.imvAvata);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent, "Please Choose App Image"),
                        1);

            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.itemUploadValue) {

//            To Do
            checkTextField();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void checkTextField() {

//        Get Value From EditText
        EditText nameEditText = getView().findViewById(R.id.edtName);
        EditText emailEditText = getView().findViewById(R.id.edtEmail);
        EditText passwordEditText = getView().findViewById(R.id.edtPassword);

        nameString = nameEditText.getText().toString().trim();
        emailString = emailEditText.getText().toString().trim();
        passwordString = passwordEditText.getText().toString().trim();

        MyAlert myAlert = new MyAlert(getActivity());

        if (chooseBool) {
//            Non Choose Image
            myAlert.normalDialog("Non Choose Image",
                    "Please Choose Image for Avata");

        } else if (nameString.isEmpty() || emailString.isEmpty() || passwordString.isEmpty()) {
            myAlert.normalDialog(getString(R.string.title_space),
                    getString(R.string.message_space));
        } else {
//            No Space
            uploadValueToFirebase();
        }


    }

    private void uploadValueToFirebase() {

//        upLoad Image
        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        StorageReference storageReference = firebaseStorage.getReference();

        StorageReference storageReference1 = storageReference.child("Avata/" + nameString + "Avata");
        storageReference1.putFile(uri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()) {
                    Log.d("5MayV1", "Upload Success");

                    findPathAvata();

                } else {
                    Log.d("5MayV1", "Upload Cannot Success ==> " + task.getException().getMessage().toString());
                }
            }
        });







    }   // uploadValue

    private void findPathAvata() {

        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        StorageReference storageReference = firebaseStorage.getReference();

        final String[] strings = new String[1];

        storageReference.child("Avata/" + nameString + "Avata")
                .getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        strings[0] = uri.toString();
                        pathAvataString = strings[0];
                        Log.d("5MayV1", "Path Avata ==> " + pathAvataString);
                        registerEmail();

                    }
                });


    }

    private void registerEmail() {

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.createUserWithEmailAndPassword(emailString, passwordString)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {

                            Log.d("5MayV1", "Register Success");

                            findUidUser();


                        } else {
                            MyAlert myAlert = new MyAlert(getActivity());
                            myAlert.normalDialog("Cannot Register",
                                    task.getException().getMessage().toString());
                        }


                    }
                });




    }

    private void findUidUser() {

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        uidUserString = firebaseUser.getUid();
        Log.d("5MayV1", "uidUser ==> " + uidUserString);

        updateNewUserToFirebase();

    }

    private void updateNewUserToFirebase() {

        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference()
                .child(uidUserString);

        UserModel userModel = new UserModel(nameString, pathAvataString);

        databaseReference.setValue(userModel).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d("5MayV1", "Success Update");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("5MayV1", "Cannot Update ==> " + e.toString());
            }
        });




    }

    private void uploadTextToFirebase() {




    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.menu_register, menu);

    }

    private void createToolbar() {

        Toolbar toolbar = getView().findViewById(R.id.toolbarRegister);
        ((MainActivity) getActivity()).setSupportActionBar(toolbar);
        ((MainActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.new_register));
        ((MainActivity) getActivity()).getSupportActionBar().setSubtitle("Please Fill All Blank");

        ((MainActivity) getActivity()).getSupportActionBar().setHomeButtonEnabled(true);
        ((MainActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        setHasOptionsMenu(true);


    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register, container, false);
        return view;
    }
}
