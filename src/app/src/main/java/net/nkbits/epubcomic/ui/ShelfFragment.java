package net.nkbits.epubcomic.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.androidcomics.acv.R;
import net.nkbits.epubcomic.Constants;
import net.nkbits.epubcomic.adapter.ItemAdapter;
import net.nkbits.epubcomic.db.DBHelper;
import net.nkbits.epubcomic.db.FileHelper;
import net.nkbits.epubcomic.db.Item;
import net.nkbits.epubcomic.view.ShelfView;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ShelfFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class ShelfFragment extends Fragment {

    private ShelfView shelfView;
    private OnFragmentInteractionListener mListener;
    private DBHelper dbHelper;
    private List<String> books;
    private List<String> comics;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param mediaType media type.
     * @return A new instance of fragment ShelfFragment.
     */
    public static ShelfFragment newInstance(int mediaType) {
        ShelfFragment fragment = new ShelfFragment();
        Bundle args = new Bundle();
        args.putInt(Constants.MEDIA_TYPE_KEY, mediaType);
        fragment.setArguments(args);

        return fragment;
    }

    public ShelfFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_shelf, container, false);

        shelfView = (ShelfView) view.findViewById(R.id.shelf_view);
        int mediaType = getArguments().getInt(Constants.MEDIA_TYPE_KEY, FileHelper.COMIC);
        update(mediaType);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void onFileSelected(String path);
    }

    public void update(int mediaType){
        if(mediaType == FileHelper.BOOK){
            books = dbHelper.file.getAllBooks();
            populateShelf(shelfView, books);
        }else if(mediaType == FileHelper.COMIC){
            comics = dbHelper.file.getAllComics();
            populateShelf(shelfView, comics);
        }
    }

    public void setDBHelper(DBHelper dbHelper){
        this.dbHelper = dbHelper;
    }

    private void populateShelf(ShelfView shelfView, List<String> items){
        ItemAdapter bookAdapter = new ItemAdapter(getActivity());

        for(int i = 0; i < items.size(); i++){
            bookAdapter.addItem(new Item());
        }

        bookAdapter.notifyDataSetChanged();
        shelfView.setAdapter(bookAdapter);
    }
}
