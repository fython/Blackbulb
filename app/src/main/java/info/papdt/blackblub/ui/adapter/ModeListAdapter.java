package info.papdt.blackblub.ui.adapter;

import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.RadioButton;
import android.widget.TextView;

import java.util.Arrays;
import java.util.List;

import info.papdt.blackblub.R;

public class ModeListAdapter extends BaseAdapter implements ListAdapter {

	private int current;

	private static final List<Pair<Integer, Integer>> MODES = Arrays.asList(
			Pair.create(R.string.mode_text_no_permission, R.string.mode_desc_no_permission),
			Pair.create(R.string.mode_text_normal, R.string.mode_desc_normal),
			Pair.create(R.string.mode_text_overlay_all, R.string.mode_desc_overlay_all),
			Pair.create(R.string.mode_text_eyes_care, R.string.mode_desc_eyes_care)
	);

	public ModeListAdapter(int current) {
		this.current = current;
	}

	public void setCurrent(int current) {
		this.current = current;
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return MODES.size();
	}

	@Override
	public Pair<Integer, Integer> getItem(int position) {
		return MODES.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_mode, parent, false);
			holder = new ViewHolder();
			holder.title = (TextView) convertView.findViewById(R.id.title);
			holder.description = (TextView) convertView.findViewById(R.id.description);
			holder.radioButton = (RadioButton) convertView.findViewById(R.id.radio_button);
			convertView.setTag(holder);
		} else  {
			holder = (ViewHolder) convertView.getTag();
		}
		holder.title.setText(getItem(position).first);
		holder.description.setText(getItem(position).second);
		holder.radioButton.setChecked(position == current);
		return convertView;
	}

	private class ViewHolder {

		TextView title, description;
		RadioButton radioButton;

	}

}
