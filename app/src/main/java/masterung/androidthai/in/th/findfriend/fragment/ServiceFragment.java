package masterung.androidthai.in.th.findfriend.fragment;

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
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import masterung.androidthai.in.th.findfriend.MainActivity;
import masterung.androidthai.in.th.findfriend.R;
import masterung.androidthai.in.th.findfriend.utility.FriendAdapter;
import masterung.androidthai.in.th.findfriend.utility.UserModel;

public class ServiceFragment extends Fragment {

    private String displayNameString, uidUserLoggedinString;
    private ArrayList<String> uidFriendStringArrayList,
            friendStringArrayList, pathAvaraStringArrayList;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

//        Create Toolbar
        createToolbar();

//        Find Member of Friend
        findMemberOfFriend();


    }   // Main Method

    private void findMemberOfFriend() {

        uidFriendStringArrayList = new ArrayList<>();
        friendStringArrayList = new ArrayList<>();
        pathAvaraStringArrayList = new ArrayList<>();

        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference();

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    Log.d("6MayV1", "dataSnapshop1 ==> " + dataSnapshot1.toString());

                    if (!uidUserLoggedinString.equals(dataSnapshot1.getKey())) {
                        uidFriendStringArrayList.add(dataSnapshot1.getKey());
                    }

                }   // for
                Log.d("5MayV1", "uidFriend ==> " + uidFriendStringArrayList.toString());

                createListView();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    private void createListView() {

        final int[] ints = new int[]{0};

        for (String childString : uidFriendStringArrayList) {

            Log.d("5MayV1", "childString " + childString);
            FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
            DatabaseReference databaseReference = firebaseDatabase.getReference()
                    .child(childString);

            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    Map map = (Map) dataSnapshot.getValue();
                    friendStringArrayList.add(String.valueOf(map.get("nameString")));
                    pathAvaraStringArrayList.add(String.valueOf(map.get("pathAvataString")));

                    Log.d("5MayV1", "friendArray ==> " + friendStringArrayList.toString());

                    if (ints[0] == uidFriendStringArrayList.size()-1) {
                        showListView();
                    }
                    ints[0] += 1;
                }   // onDataChange


                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });



        }   //for


    }   // createListView

    private void showListView() {

        Log.d("12MayV1", "StringArray ==> " + friendStringArrayList.toString());
        Log.d("12MayV1", "PathStringArray ==> " + pathAvaraStringArrayList.toString());

        FriendAdapter friendAdapter = new FriendAdapter(getActivity(),
                friendStringArrayList, pathAvaraStringArrayList);
        ListView listView = getView().findViewById(R.id.listViewFriend);
        listView.setAdapter(friendAdapter);


    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.itemSignOut) {

            signOutFirebase();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void signOutFirebase() {

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.signOut();

        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.contentMainFragment, new MainFragment())
                .commit();

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.menu_service, menu);

    }

    private void createToolbar() {
        Toolbar toolbar = getView().findViewById(R.id.toolbarService);
        ((MainActivity) getActivity()).setSupportActionBar(toolbar);
        ((MainActivity) getActivity()).getSupportActionBar().setTitle("My All Friend");

//        Show displayName on SubTitle Toolbar
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        displayNameString = firebaseUser.getDisplayName();
        uidUserLoggedinString = firebaseUser.getUid();

        ((MainActivity) getActivity()).getSupportActionBar().setSubtitle(displayNameString + " Signed");



        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_service, container, false);
        return view;
    }
}
