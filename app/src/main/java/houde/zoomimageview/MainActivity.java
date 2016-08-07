package houde.zoomimageview;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import houde.zoomimageview.view.ZooImageView;

public class MainActivity extends AppCompatActivity {


    private ViewPager vp;

    private int[] imgs = new int[]{R.mipmap.a, R.mipmap.b, R.mipmap.c};
    private ImageView[] imageViews = new ImageView[imgs.length];


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        vp = (ViewPager) findViewById(R.id.vp);

        vp.setAdapter(new PagerAdapter() {


            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                ZooImageView imageView = new ZooImageView(getApplicationContext());
                imageView.setImageResource(imgs[position]);
                container.addView(imageView);
                imageViews[position] = imageView;
                return imageView;
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                container.removeView(imageViews[position]);
            }

            @Override
            public int getCount() {
                return imageViews.length;
            }

            @Override
            public boolean isViewFromObject(View view, Object object) {
                return view == object;
            }
        });
    }
}
