package sb.app.sbviewpager

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.viewpager.widget.ViewPager
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems
import sb.app.sbviewpager.library.SbTabView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val adapter = FragmentPagerItemAdapter(
            supportFragmentManager, FragmentPagerItems.with(this)
                .add("titleA", Home::class.java)
                .add("titleB", Watch::class.java)
                .add("titleB", Profile::class.java)
                .add("titleB", Friends::class.java)

                .create())


     val sbTabView =   findViewById<SbTabView>(R.id.sbTabView)


        val viewPager = findViewById<ViewPager>(R.id.viewPager)

        viewPager.adapter = adapter

        sbTabView.setViewPager(viewPager)

    }
}