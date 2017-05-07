/**
 * Copyright (C) 2017 Gustav Wang
 */

package carbonylgroup.com.schoolpower.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.ramotion.foldingcell.FoldingCell;

import java.util.ArrayList;
import java.util.HashSet;

import carbonylgroup.com.schoolpower.R;
import carbonylgroup.com.schoolpower.activities.MainActivity;
import carbonylgroup.com.schoolpower.classes.Transition.DetailsTransition;
import carbonylgroup.com.schoolpower.classes.Adapter.FoldingCellListAdapter;
import carbonylgroup.com.schoolpower.classes.ListItems.MainListItem;
import carbonylgroup.com.schoolpower.classes.Transition.TransitionHelper;
import carbonylgroup.com.schoolpower.classes.Utils.Utils;
import carbonylgroup.com.schoolpower.classes.Utils.postData;


public class HomeFragment extends TransitionHelper.BaseFragment {

    private int transformedPosition = -1;

    private View view;
    private Animation fab_out;
    private Animation fab_in;
    private CourseDetailFragment courseDetailFragment;
    private HashSet<Integer> unfoldedIndexesBackUp = new HashSet<>();
    private ArrayList<MainListItem> dataList;
    private FoldingCellListAdapter adapter;
    private Utils utils;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.home_view_content, container, false);

        initAnim();
        initValue();

        new Thread(new postData(
                getString(R.string.postURL),
                "username=19050069IBO&password=albb2688",
                new Handler() {
                    @Override
                    public void handleMessage(Message msg) {

                        String jsonStr = msg.obj.toString().isEmpty() ? "" : msg.obj.toString();
                        if(!jsonStr.equals("")) {
                            try {
                                utils.outputDataJson(jsonStr);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            dataList = utils.parseJsonResult(jsonStr);
                            refreshAdapter();
                        }
                    }
                })).start();

        return view;
    }

    private void initAnim() {

        fab_in = new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        fab_in.setDuration(200);
        fab_in.setInterpolator(new DecelerateInterpolator());
        fab_out = new ScaleAnimation(1.0f, 0.0f, 1.0f, 0.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        fab_out.setDuration(200);
        fab_out.setInterpolator(new DecelerateInterpolator());
    }

    private void initValue() {

        utils = new Utils(getActivity());
        dataList = new ArrayList<>();
        MainActivity.of(getActivity()).setPresentFragment(0);
        MainActivity.of(getActivity()).setToolBarElevation(utils.dpToPx(4));
        MainActivity.of(getActivity()).setToolBarTitle(getString(R.string.dashboard));

        try {
            if (utils.inputDataArrayList() != null) {
                dataList = utils.inputDataArrayList();
                initAdapter();
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void initAdapter() {

        final ListView theListView = (ListView) view.findViewById(R.id.mainListView);

        adapter = new FoldingCellListAdapter(getActivity(), dataList, unfoldedIndexesBackUp, transformedPosition);

        adapter.setDefaultRequestBtnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                MainActivity.of(getActivity()).setMainListItemTransporter(dataList.get(theListView.getPositionForView(v)));

                if (transformedPosition != -1) {

                    View itemView = getItemViewByPosition(transformedPosition, theListView);
                    itemView.findViewById(R.id.unfold_header_view).setTransitionName("");
                    itemView.findViewById(R.id.detail_subject_title_tv).setTransitionName("");
                }

                transformedPosition = theListView.getPositionForView(v);
                View itemView = getItemViewByPosition(theListView.getPositionForView(v), theListView);
                itemView.findViewById(R.id.floating_action_button).startAnimation(fab_out);
                itemView.findViewById(R.id.floating_action_button).setVisibility(View.GONE);
                gotoCourseDetail(itemView.findViewById(R.id.unfold_header_view), itemView.findViewById(R.id.detail_subject_title_tv), transformedPosition);
            }
        });

        theListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l) {

                adapter.registerToggle(pos);
                ((FoldingCell) view).toggle(false);
                adapter.refreshPeriodRecycler((FoldingCell) view, pos);
                unfoldedIndexesBackUp = adapter.getUnfoldedIndexes();
            }
        });

        theListView.setAdapter(adapter);
    }

    private void refreshAdapter() {

        adapter.setMainListItems(dataList);
        adapter.notifyDataSetChanged();
        Toast.makeText(getActivity(), "Data Updated", Toast.LENGTH_SHORT).show();
    }

    private View getItemViewByPosition(int position, ListView listView) {

        int firstItemPos = listView.getFirstVisiblePosition();
        int lastItemPos = listView.getLastVisiblePosition();

        if (position < firstItemPos || position > lastItemPos)
            return listView.getAdapter().getView(position, null, listView);
        else
            return listView.getChildAt(position - firstItemPos);
    }

    private void gotoCourseDetail(View _header, View _subject_title, int transformedPosition) {

        courseDetailFragment = new CourseDetailFragment();
        courseDetailFragment.setSharedElementEnterTransition(new DetailsTransition());
        courseDetailFragment.setSharedElementReturnTransition(new DetailsTransition());

        _header.setTransitionName(getString(R.string.shared_element_course_header));
        _subject_title.setTransitionName(getString(R.string.shared_element_course_subject_title));

        Bundle bundle = new Bundle();
        bundle.putInt("transformedPosition", transformedPosition);
        courseDetailFragment.setArguments(bundle);

        getActivity().getFragmentManager()
                .beginTransaction()
                .addSharedElement(_header, getString(R.string.shared_element_course_header))
                .addSharedElement(_subject_title, getString(R.string.shared_element_course_subject_title))
                .setCustomAnimations(R.animator.do_nothing, R.animator.fade_out)
                .replace(R.id.content_view, courseDetailFragment)
                .addToBackStack(null)
                .commit();

        MainActivity.of(getActivity()).setPresentFragment(1);
        MainActivity.of(getActivity()).animateDrawerToggle(true);
        MainActivity.of(getActivity()).setToolBarElevation(0);
    }

    private void preRenderUnfoldCells() {

        final ListView theListView = (ListView) view.findViewById(R.id.mainListView);
        for (int i = 0; i < 10; i++) {

            View itemView = getItemViewByPosition(transformedPosition, theListView);
            ((FoldingCell) itemView).toggle(false);
            ((FoldingCell) itemView).toggle(false);
        }
    }

    @Override
    public void onAfterEnter() {
        super.onAfterEnter();
        preRenderUnfoldCells();
    }
}