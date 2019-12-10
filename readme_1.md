1.

这个demo的ItemDecoration好像不太管用，不要看这个。

DividerItemDecoration extends RecyclerView.ItemDecoration

继承RecyclerView.ItemDecoration，可以控制每个item之间的间隔

重写 onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) 

重写 getItemOffsets

// 添加ItemDecoration

```
RecyclerView.addItemDecoration(mDecoration)
```

// 删除ItemDecoration

```
mRecyclerView.removeItemDecoration(mDecoration);
```



2.

线性布局；可横向可纵向；数据可反转layout

```
LinearLayoutManager(Context context, int orientation, boolean reverseLayout)

---------------------------------------

mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
```

设置线性布局

```
RecyclerView.setLayoutManager(mLayoutManager);
```



3.

// model类，基础数据结构

```
public class ObjectModel {
    public String title;
    public int number;
}
```

// 全局数据就一份，需要数据的，就拿着list的引用

```
private ArrayList<ObjectModel> mData;
```



4.

// view类，继承RecyclerView.ViewHolder，基础ViewHolder类

// 只需要定义需要哪些view

```
    public static class VH extends RecyclerView.ViewHolder {
        public final TextView title;
        public final TextView number;

        public VH(View v) {
            super(v);
            title = (TextView) v.findViewById(R.id.title);
            number = (TextView) v.findViewById(R.id.number);
        }
    }
```



5.

// adapter类，model数据 和 viewHolder 绑定的地方

```
public class NormalAdapter extends RecyclerView.Adapter<NormalAdapter.VH>
```

// 实现下面方法

// onBindViewHolder 拿到holder，根据position获取对应的数据，给holder塞数据

// getItemCount          返回数据大小

// createViewHolder   创建ViewHolder，这里会inflate view，耗时；

//                         		   recyclerView只会加载需要的view，创建次数是不多的，它会复用ViewHolder的

```
@Override
public void onBindViewHolder(VH holder, int position) {
    ObjectModel model = mDatas.get(position);
    holder.number.setText(model.number + "");
    holder.title.setText(model.title);
    holder.itemView.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //item 点击事件
        }
    });
}

@Override
public int getItemCount() {
    return mDatas.size();
}

@Override
public VH onCreateViewHolder(ViewGroup parent, int viewType) {
    View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_1, parent, false);
    return new VH(v);
}
```

// 给RV设置adapter

```
mRecyclerView.setAdapter(mAdapter);
```



6.

如何实现header和footer；如何通过不同的type创建不同的ViewHolder

// 写一个NormalAdapterWrapper

//		成员变量 `View mHeaderView` 和 `View mFooterView`

//		封装`NormalAdapter mAdapter`

```
public class NormalAdapterWrapper extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

		private NormalAdapter mAdapter;
    private View mHeaderView;
    private View mFooterView;
    
}
```

// 定义数据type

```
enum ITEM_TYPE {
    HEADER,
    FOOTER,
    NORMAL
}
```

// 重写getItemCount：前后两个view + adapter的数量

```
@Override
public int getItemCount() {
    return mAdapter.getItemCount() + 2;
}
```

// 重写getItemViewType：根据postion，判断是第一个还是最后一个，返回不同的type

// 枚举.ordinal ：表示枚举的序号

```
@Override
public int getItemViewType(int position) {
    if (position == 0) {
        return ITEM_TYPE.HEADER.ordinal();
    } else if (position == mAdapter.getItemCount() + 1) {
        return ITEM_TYPE.FOOTER.ordinal();
    } else {
        return ITEM_TYPE.NORMAL.ordinal();
    }
}
```

// 根据type，创建不同的viewHolder

```
@Override
public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    if (viewType == ITEM_TYPE.HEADER.ordinal()) {
        return new RecyclerView.ViewHolder(mHeaderView) {
        };
    } else if (viewType == ITEM_TYPE.FOOTER.ordinal()) {
        return new RecyclerView.ViewHolder(mFooterView) {
        };
    } else {
        return mAdapter.onCreateViewHolder(parent, viewType);
    }
}
```



7.

如何添加删除数据

// notifyItemInserted 添加数据

//		直接给数据list，add数据即可，adapter是拿着list的引用的，然后调用adapter的notifyItemInserted

//		给个position参数，数据就添加到固定位置上了

//		有position的好处是：可以只刷新固定位置的，不用全部遍历

```
ObjectModel obj = new ObjectModel();
obj.number = 0;
obj.title = "Insert";
mData.add(0, obj);
mAdapter.notifyItemInserted(1);
```

// notifyItemRemoved  删除数据

```
mData.remove(0);
mAdapter.notifyItemRemoved(1);
```

// notifyDataSetChanged ：它会强制跟新所有数据以及布局，耗时

```
mAdapter.notifyDataSetChanged();
```

