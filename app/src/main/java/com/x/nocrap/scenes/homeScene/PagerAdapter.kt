package com.x.nocrap.scenes.homeScene

import android.os.Parcelable
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import java.util.*

class PagerAdapter(manager: FragmentManager) : FragmentStatePagerAdapter(manager) {
    private val mFragmentList: MutableList<Fragment> =
        ArrayList()
    private val mFragmentTitleList: MutableList<String> =
        ArrayList()
    var list1 = ArrayList<VideoEntity>()
    //boolean hasDummyFrag = false;

    //boolean hasDummyFrag = false;
    init {
        mFragmentList.clear()
        mFragmentTitleList.clear()
    }

    fun addList(list: ArrayList<VideoEntity>) {
        this.list1.addAll(list)
    }

    fun getList(): ArrayList<VideoEntity> {
        return list1
    }

    override fun restoreState(state: Parcelable?, loader: ClassLoader?) {
        try {
            super.restoreState(state, loader)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getItemPosition(`object`: Any): Int {
        return androidx.viewpager.widget.PagerAdapter.POSITION_NONE
    }
    /*fun getItemPosition(obj: Any?): Int {
        // refresh all fragments when data set changed
        return com.yoo.shortvideos.scenes.mainScene.fragments.home.PagerAdapter.POSITION_NONE
    }*/

    fun removePages() {
        mFragmentList.clear()
        list1.clear()
        mFragmentTitleList.clear()
        notifyDataSetChanged()
    }

    fun removeDummyPage() {
        //  hasDummyFrag = false;
        mFragmentList.removeAt(mFragmentList.size - 1)
        mFragmentTitleList.removeAt(mFragmentTitleList.size - 1)
        notifyDataSetChanged()
    }


    /*public boolean isDummyFrag(int position) {
    return  hasDummyFrag && position == mFragmentList.size() -1 && position == list.size() ;
    }*/


    override fun getItem(position: Int): Fragment {

        return mFragmentList[position]
    }

    override fun getCount(): Int {
        return mFragmentList.size
    }

    fun addFrag(
        fragment: Fragment,
        title: String,
        entity: VideoEntity
    ) {
        mFragmentList.add(fragment)
        mFragmentTitleList.add(title)
        list1.add(entity)
    }

    /*public void addFragDummy(Fragment fragment, String title) {
        hasDummyFrag = true;
        mFragmentList.add(fragment);
        mFragmentTitleList.add(title);
    }*/

    /*public void addFragDummy(Fragment fragment, String title) {
        hasDummyFrag = true;
        mFragmentList.add(fragment);
        mFragmentTitleList.add(title);
    }*/
    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val frag =
            super.instantiateItem(container, position) as Fragment
        mFragmentList[position] = frag
        return frag
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return mFragmentTitleList[position]
    }

}
