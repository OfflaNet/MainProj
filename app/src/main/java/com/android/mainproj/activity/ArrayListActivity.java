package com.android.mainproj.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.android.mainproj.R;
import com.android.mainproj.log.LogService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ArrayListActivity extends AppCompatActivity
{
    private Activity activity;

    private ImageButton btn_array_back;

    private EditText et_array_item;

    private Button btn_array_item_add, btn_array_item_edit, btn_array_item_del;

    private ListView lv_array;

    private ArrayAdapter arrayAdapter;
    
    private List<String> itemList = new ArrayList<String>(Arrays.asList("첫 번째", "두 번째"));

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        try
        {
            setContentView(R.layout.activity_array);

            init();

            setting();

            addListener();
        }
        catch (Exception ex)
        {
            LogService.error(this, ex.getMessage(), ex);
        }
    }

    private void init()
    {
        activity = this;

        et_array_item = findViewById(R.id.et_array_item);

        btn_array_back = findViewById(R.id.btn_array_back);

        btn_array_item_add = findViewById(R.id.btn_array_item_add);

        btn_array_item_edit = findViewById(R.id.btn_array_item_edit);

        btn_array_item_del = findViewById(R.id.btn_array_item_del);

        lv_array = findViewById(R.id.lv_array);

        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_single_choice, itemList);
    }

    private void setting()
    {
        lv_array.setAdapter(arrayAdapter);

        lv_array.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
    }

    private void addListener()
    {
        btn_array_back.setOnClickListener(listener_back);

        btn_array_item_add.setOnClickListener(listener_item_add);

        btn_array_item_edit.setOnClickListener(listener_item_edit);

        btn_array_item_del.setOnClickListener(listener_item_del);

        lv_array.setOnItemClickListener(listener_item_click);
    }

    private AdapterView.OnItemClickListener listener_item_click = new AdapterView.OnItemClickListener()
    {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id)
        {
            String item = itemList.get(position);
            Toast.makeText(activity, position + " : " + item, Toast.LENGTH_SHORT).show();
        }
    };

    private View.OnClickListener listener_back = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            finish();
        }
    };

    private View.OnClickListener listener_item_add = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            String item = et_array_item.getText().toString();

            if(item.equals(""))
            {
                Toast.makeText(activity, "추가할 아이템 이름을 입력해주세요.", Toast.LENGTH_SHORT).show();
            }

            itemList.add(item);

            arrayAdapter.notifyDataSetChanged();
        }
    };

    private View.OnClickListener listener_item_edit = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            int index = lv_array.getCheckedItemPosition();

            String item = et_array_item.getText().toString();

            if(index < 0)
            {
                Toast.makeText(activity, "편집 대상을 선택하세요.", Toast.LENGTH_SHORT).show();
            }
            else if(item.isEmpty())
            {
                Toast.makeText(activity, "수정할 아이템 이름을 입력해주세요.", Toast.LENGTH_SHORT).show();
            }
            else
            {
                itemList.set(index, item);
                lv_array.clearChoices();
                arrayAdapter.notifyDataSetChanged();
            }
        }
    };

    private View.OnClickListener listener_item_del = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            int index = lv_array.getCheckedItemPosition();

            if(index < 0)
            {
                Toast.makeText(activity, "삭제 대상을 선택하세요.", Toast.LENGTH_SHORT).show();
            }
            else
            {
                itemList.remove(index);
                lv_array.clearChoices();
                arrayAdapter.notifyDataSetChanged();
            }
        }
    };
}