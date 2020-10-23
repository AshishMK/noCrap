package com.x.nocrap.utils

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.databinding.ObservableInt
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.x.nocrap.R
import com.x.nocrap.application.AppController
import com.x.nocrap.data.local.pref.SharedPrefStorage
import com.x.nocrap.databinding.AlertDialogBinding
import com.x.nocrap.databinding.DialogItemBinding
import com.x.nocrap.databinding.DialogItemMultiBinding
import java.util.*


/**
 * {@link DialogFragment} class to provide different types of Dialog to application wide
 */
class AlertDialogProvider : DialogFragment() {
    companion object {
        var TYPE_EDIT = 0
        public var TYPE_NORMAL = 1
        var TYPE_LIST = 2
        var TYPE_MULTI_LIST = 6
        var TYPE_EDIT_LINK = 3
        var TYPE_NORMAL_DONT = 4
        var TYPE_PROGRESS_DEFINITE = 5
        var TYPE_UPDATE = 7
        var TYPE_UPDATE_FORCE = 8
        var TYPE_EDIT_BIG = 9
        const val TITLE_DIALOG = "title_dialog"
        const val MESSAGE_DIALOG = "message_dialog"
        const val LIST_DIALOG = "list_dialog"
        const val LIST_DIALOG_DRAWABLES = "list_dialog_drawables"
        const val DIALOG_TYPE = "dialog_type"
        const val LIST_DIALOG_SELECTED = "list_dialog_selected"
        fun getInstance(
            title: String,
            message: String,
            dialog_type: Int,
            showCancelDownload: Boolean
        ): AlertDialogProvider {
            val fragment = AlertDialogProvider()
            val args = Bundle()
            args.putInt(DIALOG_TYPE, dialog_type)
            args.putBoolean("showCancelDownload", showCancelDownload)
            args.putString(TITLE_DIALOG, title)
            args.putString(MESSAGE_DIALOG, message)
            fragment.arguments = args
            return fragment
        }

        fun getInstance(
            title: String,
            items: ArrayList<String>,
            selectedItems: ArrayList<String>,
            showCancelDownload: Boolean
        ): AlertDialogProvider {
            val fragment = AlertDialogProvider()
            val args = Bundle()
            args.putInt(DIALOG_TYPE, TYPE_MULTI_LIST)
            args.putString(TITLE_DIALOG, title)
            args.putStringArrayList(LIST_DIALOG, items)
            args.putStringArrayList(LIST_DIALOG_SELECTED, selectedItems)
            args.putBoolean("showCancelDownload", showCancelDownload)
            fragment.arguments = args
            return fragment
        }

        fun getInstance(
            title: String,
            items: ArrayList<String>,
            drawables: ArrayList<Int>,
            dialog_type: Int,
            showCancelDownload: Boolean
        ): AlertDialogProvider {
            val fragment = AlertDialogProvider()
            val args = Bundle()
            args.putInt(DIALOG_TYPE, dialog_type)
            args.putString(TITLE_DIALOG, title)
            args.putIntegerArrayList(LIST_DIALOG_DRAWABLES, drawables)
            args.putBoolean("showCancelDownload", showCancelDownload)
            args.putStringArrayList(LIST_DIALOG, items)
            fragment.arguments = args
            return fragment
        }
    }

    var progress = ObservableInt(0)
    var showCancelDownload = true
    var varificationMessage: Int = R.string.val_sel_item
    var useListDivider = false
    fun useListdivider(use: Boolean): AlertDialogProvider {
        useListDivider = use
        return this
    }

    fun setAlertDialogListener(alertDialogListener: AlertDialogListener): AlertDialogProvider {
        this.alertDialogListener = alertDialogListener
        return this
    }

    var title = ObservableField<String>()
    var message: String? = null
    var dialog_type = 0
    var items = ArrayList<String>()
    var selectedItems = ArrayList<String>()
    var item_drawables = ArrayList<Int>()

    /*
     * I am using DataBinding
     * */
    private lateinit var binding: AlertDialogBinding
    var alertDialogListener: AlertDialogListener? = null
    var alertDialogItemListener: AlertDialogItemListener? = null
    var alertDialogMultiItemListener: AlertDialogMultiItemListener? = null


    interface AlertDialogListener {
        fun onDialogCancel()
        fun onDialogOk(text: String, dialog: AlertDialogProvider)
    }

    interface AlertDialogItemListener {
        fun onItemClicked(position: Int)
    }

    interface AlertDialogMultiItemListener {
        fun onDoneClicked(selectedItems: ArrayList<String>)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog!!.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        println("cccnuLL " + (container == null))
        initialiseView(container)
        dialog!!.setCanceledOnTouchOutside(false)
        return binding.getRoot()
    }

    /*
     * Initialising the View using Data Binding
     * */
    private fun initialiseView(viewGroup: ViewGroup?) {
        binding = DataBindingUtil.inflate(
            LayoutInflater.from(activity),
            R.layout.alert_dialog_fragment,
            viewGroup,
            false
        )
        binding.title = title
        binding.content = this
        binding.fileProgress = progress
        binding.preferenceStorage = SharedPrefStorage(AppController.getInstance())
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)


        binding.ok.setText(if (dialog_type == TYPE_EDIT_LINK || dialog_type == TYPE_EDIT_BIG || dialog_type == TYPE_EDIT) R.string.submit else if (dialog_type == TYPE_PROGRESS_DEFINITE) R.string.ok_cancel_download else if (dialog_type == TYPE_UPDATE_FORCE || dialog_type == TYPE_UPDATE) R.string.update else R.string.ok)
        if (dialog_type != TYPE_NORMAL) {
        } else {
            if (dialog_type == TYPE_UPDATE_FORCE) {
                binding.ok.setText(R.string.update)
                binding.cancel.visibility = View.GONE
            }
            if (dialog_type == TYPE_UPDATE) {
                binding.ok.setText(R.string.update)
            }
        }
        if (dialog_type == TYPE_LIST) {
            setUplist()
        } else if (dialog_type == TYPE_MULTI_LIST) {
            //setUpMultiList()
        } else if (dialog_type == TYPE_PROGRESS_DEFINITE) {
            binding.cancelDownload.visibility = if (showCancelDownload) View.VISIBLE else View.GONE
            binding.dummy.visibility = if (showCancelDownload) View.GONE else View.VISIBLE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            title.set(arguments!!.getString(TITLE_DIALOG))
            message = arguments!!.getString(MESSAGE_DIALOG)
            showCancelDownload = arguments!!.getBoolean("showCancelDownload")
            dialog_type = arguments!!.getInt(DIALOG_TYPE)
            if (arguments!!.containsKey(LIST_DIALOG))
                items = arguments!!.getStringArrayList(LIST_DIALOG)!!
            if (arguments!!.containsKey(LIST_DIALOG_SELECTED))
                selectedItems =
                    arguments!!.getStringArrayList(LIST_DIALOG_SELECTED)!!
            if (arguments!!.containsKey(LIST_DIALOG_DRAWABLES))
                item_drawables =
                    arguments!!.getIntegerArrayList(LIST_DIALOG_DRAWABLES)!!
        }
        if (dialog_type != TYPE_NORMAL) {
            setStyle(STYLE_NO_TITLE, R.style.DialogStyle)
        } else {
            setStyle(STYLE_NO_TITLE, R.style.DialogStyle)
        }
    }

    fun setUplist() {
        binding.list.layoutManager = LinearLayoutManager(activity)
        binding.list.adapter = object :
            RecyclerView.Adapter<ListViewHolder>() {
            override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ListViewHolder {
                return ListViewHolder(
                    DataBindingUtil.inflate(
                        LayoutInflater.from(activity), R.layout.dialog_list_item, viewGroup, false
                    )
                )
            }

            override fun onBindViewHolder(listViewHolder: ListViewHolder, i: Int) {
                listViewHolder.bindTo(
                    items[i],
                    if (item_drawables.size == 0) 0 else item_drawables[i],
                    listViewHolder,
                    i
                )
            }

            override fun getItemCount(): Int {
                return items.size
            }


        }
    }


    inner class ListViewHolder(private val binding: DialogItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindTo(
            content: String,
            src: Int,
            holder: ListViewHolder,
            position: Int
        ) {
            binding.contentViewHolder = holder
            binding.content = content
            binding.position = position
            binding.useDivider = (if (!useListDivider) false else position < items.size - 1)
            binding.src = src
        }

        fun onItemClick(position: Int) {
            dismiss()
            if (alertDialogItemListener != null) {
                alertDialogItemListener!!.onItemClicked(position)
            }
        }


    }


    fun setUpMultiList() {
        binding.list.layoutManager = LinearLayoutManager(activity)
        binding.list.addItemDecoration(
            DividerItemDecoration(
                activity,
                DividerItemDecoration.VERTICAL
            )
        )
        binding.list.adapter = object : RecyclerView.Adapter<ListViewMultiHolder>() {
            override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ListViewMultiHolder {
                return ListViewMultiHolder(
                    DataBindingUtil.inflate(
                        LayoutInflater.from(activity),
                        R.layout.dialog_list_multi_item,
                        viewGroup,
                        false
                    )
                )
            }

            override fun onBindViewHolder(listViewHolder: ListViewMultiHolder, i: Int) {
                listViewHolder.bindTo(items[i], listViewHolder, i)
            }

            override fun getItemCount(): Int {
                return items.size
            }
        }
    }

    inner class ListViewMultiHolder(private val binding: DialogItemMultiBinding) :
        RecyclerView.ViewHolder(binding.root) {
        var isSelected = ObservableBoolean(false)

        init {
            binding.isSelected = isSelected
        }

        fun bindTo(
            content: String,
            holder: ListViewMultiHolder,
            position: Int
        ) {
            binding.contentViewHolder = holder
            binding.content = content
            binding.content = content
            binding.position = position
            isSelected.set(selectedItems.contains(content.toLowerCase()))
        }

        fun onItemClick(position: Int) {
            //AlertDialogProvider.this.dismiss();
            if (selectedItems.contains(items[position].toLowerCase())) {
                selectedItems.remove(items[position].toLowerCase())
            } else {
                selectedItems.add(items[position].toLowerCase())
            }
            isSelected.set(selectedItems.contains(items[position].toLowerCase()))
        }
    }

    fun onDialogCancel(v: View) {
        dismiss()
        if (alertDialogListener != null) {
            alertDialogListener!!.onDialogCancel()
        }
    }

    fun onDialogOk(v: View) {
        if (dialog_type != TYPE_EDIT_BIG && dialog_type != TYPE_EDIT_LINK && dialog_type != TYPE_EDIT) {
            dismiss()
        }
        if (alertDialogListener != null) {
            alertDialogListener!!.onDialogOk(binding.editText.text.toString(), this)
        }
    }

    fun setVarificationMessage(@StringRes varifyMessage: Int): AlertDialogProvider {
        varificationMessage = varifyMessage
        return this
    }

    fun onDoneClickListener(v: View) {
        if (selectedItems.size == 0) {
            Toast.makeText(activity, varificationMessage, Toast.LENGTH_SHORT).show()
            return
        }
        dismiss()
        if (alertDialogMultiItemListener != null) {
            alertDialogMultiItemListener!!.onDoneClicked(selectedItems)
        }
    }

}