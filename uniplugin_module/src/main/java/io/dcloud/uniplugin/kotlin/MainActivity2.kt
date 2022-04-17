package io.dcloud.uniplugin.kotlin

import android.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View

import android.widget.ImageView
import uni.dcloud.io.uniplugin_module.R
import java.util.*


class MainActivity2 : AppCompatActivity() {

    private var imageView: ImageView? = null
    private var pieView: PieView? = null
    private var isRunning = false
    private val i = intArrayOf(0,1, 2,3,4, 5,6, 7,8)

    private val mStrings = arrayOf(
        "打10下屁股",
        "一个草莓蛋糕",
        "罚款100元",
        "10根烤肠",
        "无条件原谅",
        "一顿火锅",
        "带我去美甲",
        "磕三个头",
        "带我吃饭",
        "眼影盘",
        "转账520",
        "一杯奶茶",
        "跳脱衣舞",
        "一瓶香水",
        "发朋友圈承认错误",
        "跪搓衣板",
        "清3样购物车",
        "写1000个我爱你",
        "任我摆布一晚上",
        "迪奥999"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        imageView = findViewById(R.id.image)
        pieView = findViewById(R.id.zpan)
//        val title = findViewById<TextView>(R.id.tv_title)
//        title.text = "王者荣耀抽英雄啦"

        pieView?.setStrings(mStrings)

        pieView?.setListener(PieView.RotateListener { s ->
            isRunning = false
            AlertDialog.Builder(this@MainActivity2)
                .setMessage("你抽中了：" + s)
                .setNegativeButton("退出", null)
                .show()
        })

        imageView?.setOnClickListener(View.OnClickListener {
            if (!isRunning) {
                val random = Random()
                pieView?.rotate(i[random.nextInt(i.size)]);
//                pieView?.rotate(1)
            }
            isRunning = true
        })
    }

}