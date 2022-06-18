package sb.app.sbviewpager.library

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateInterpolator
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.XmlRes
import androidx.viewpager.widget.ViewPager
import sb.app.sbviewpager.R

class SbTabView @JvmOverloads constructor(context : Context,
                                          attr : AttributeSet?=null,
                                          defStyle:Int =0
                                            ) : View(context ,attr ,defStyle) , ViewPager.OnPageChangeListener {

    private var items = listOf<BottomBarItem>()

    companion object {

        private const val INVALID_RES = -1
        private const val DEFAULT_MIN_TAB_HEIGHT :Float  = 50f
        private const val DEFAULT_TEXT_SIZE =16f
        private const val DEFUALT_INDICATOR_SIZE = 3f
        private const val DEFAULT_DURATION_ANIMATION = 500L

    }


    private fun d2s( value:Float ):Float{
        val displayMetrics = context.resources.displayMetrics
     return     TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP ,value ,displayMetrics) }

    private fun d2p(value : Float):Float{
      val displayMetrics =  context.resources.displayMetrics
       return   TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP ,value ,displayMetrics) }


    private var _deactiveColor = Color.LTGRAY
    private var deactiveColor  @ColorInt get() = _deactiveColor
    set(@ColorInt value) {
        _deactiveColor = value
        invalidate() }


    private var _minTabHeight = d2p(DEFAULT_MIN_TAB_HEIGHT)
    private var minTabHeight : Float @Dimension get()=_minTabHeight
    set(@Dimension value) {
        _minTabHeight = value
         invalidate() }

    @XmlRes
    private var _itemMenuRes :Int = INVALID_RES

    private var _containerColor = Color.RED
    private var containerColor :Int @ColorInt get() = _containerColor
   set(@ColorInt value) {
       _containerColor = value
       invalidate() }

    private var _indicatorSize = d2p(DEFUALT_INDICATOR_SIZE)
    private var _indicatorColor = Color.BLUE

    init { obtainStyleableAttributes(context , attr,defStyle) }

    private var indicatorColor @ColorInt  get() = _indicatorColor
        set(@ColorInt value) {
            _indicatorColor = value
            invalidate()
        }



    private var indicatorTranslateX = 0f

    private val indicatorPaint = Paint().apply {
        this.isAntiAlias = true
        this.color  = indicatorColor

    }


    private var itemMenuRes : Int @XmlRes get()  = _itemMenuRes
        set(@XmlRes value) {

            _itemMenuRes = value
            if(value!= INVALID_RES){
                items = BottomBarParser(context ,value).parse()
            }
        }

    private val containerPaint = Paint().apply{
        this.color = containerColor
        this.isAntiAlias = true
        this.style = Paint.Style.FILL }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val widthSize =   MeasureSpec.getSize(widthMeasureSpec).toFloat()
        val heightSize = MeasureSpec.getSize(heightMeasureSpec).toFloat()
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)

        val height = when(heightMode){
            MeasureSpec.EXACTLY -> heightSize
            MeasureSpec.AT_MOST -> Math.min(minTabHeight ,heightSize)
            MeasureSpec.UNSPECIFIED -> heightSize
            else -> heightSize }

         setMeasuredDimension(widthSize.toInt() ,height.toInt())
    }




    private fun obtainStyleableAttributes(context: Context ,
                                          attr: AttributeSet? ,
                                          defStyle: Int) {


        val typedArray = context.obtainStyledAttributes(attr , R.styleable.SbViewPager ,defStyle ,0)


        try
        {


            itemMenuRes = typedArray.getResourceId(R.styleable.SbViewPager_sb_menu  , itemMenuRes)

            containerColor = typedArray.getColor(R.styleable.SbViewPager_sb_containerColor ,containerColor)

            minTabHeight = typedArray.getDimension(R.styleable.SbViewPager_sb_minHeight ,minTabHeight)

            indicatorSize = typedArray.getDimension(R.styleable.SbViewPager_sb_indicatorSize ,indicatorSize)

            duration = typedArray.getInt(R.styleable.SbViewPager_sb_duration ,duration.toInt()).toLong()

            indicatorColor = typedArray.getColor(R.styleable.SbViewPager_sb_indicatorColor ,indicatorColor)

            deactiveColor = typedArray.getColor(R.styleable.SbViewPager_sb_deactiveColor ,deactiveColor)



        }catch (e:Exception){

            e.stackTrace

        }finally {
            typedArray.recycle() }

    }

   private var itemWidth  : Int = 0

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        itemWidth = width / items.size

        var translateX  = 0f

        for((index ,item) in items.withIndex()){
            item.rect.left = translateX
            item.rect.right = translateX + itemWidth
            item.rect.top = 0f
            item.rect.bottom = height.toFloat()
            translateX +=itemWidth .toFloat()

        } }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        if(canvas == null ) return

        createContainer(canvas)
        for((index ,item) in items.withIndex()){
            drawText(item.title ,canvas ,item.rect,index ) }

        createIndicator(canvas) }


    private var indicatorSize :Float @Dimension get()   = _indicatorSize
    set(@Dimension value) {

        _indicatorSize = value
        invalidate() }

    private val indicatorRect = RectF()

    private fun createIndicator(canvas: Canvas) {
        indicatorRect.left = indicatorTranslateX
        indicatorRect.right = indicatorTranslateX + itemWidth
        indicatorRect.top = height.toFloat() - indicatorSize
        indicatorRect.bottom = height.toFloat() + indicatorSize

        canvas.drawRect( indicatorRect, indicatorPaint)
    }

    private var _itemTextSize = d2s(DEFAULT_TEXT_SIZE)
    private var itemTextSize @Dimension get() =_itemTextSize
    set(@Dimension  value) {

        _itemTextSize =value
        invalidate() }

    private var activeItemIndex = 0

    private val paintText = Paint().apply {
        this.isAntiAlias = true
        this.color = Color.BLACK
         this.textSize = itemTextSize
    }

    private fun drawText(title:String  ,canvas: Canvas ,itemRect: RectF , index :Int ) {
        val textHeight = (paintText.descent() + paintText.ascent())/2
        val textLength = paintText.measureText(title)

        paintText.color =  if(activeItemIndex == index) indicatorColor  else deactiveColor

        canvas.drawText(title ,
            itemRect.centerX() - textLength/2  ,
            itemRect.centerY() - textHeight ,
            paintText  )
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {

        if(event==null ) return false

        when(event.action){

            MotionEvent.ACTION_DOWN -> return true

            MotionEvent.ACTION_UP ->{

                for((index,item) in items.withIndex()){

                    if(items[index].rect.contains(event.x ,event.y)){

                        activeItemIndex = index
                        viewPager?.currentItem = index
                        animate(index)


                    }
                }


            }


        }



        return super.onTouchEvent(event)



    }

    private fun createContainer(canvas: Canvas) {
        val containerRect = Rect(0,0 ,  width ,height )
        canvas.drawRect(containerRect ,containerPaint) }

    private var viewPager :ViewPager?=null

    fun setViewPager(viewPager: ViewPager) {
        this.viewPager = viewPager
        viewPager.addOnPageChangeListener(this ) }

    override fun onPageScrolled(position: Int ,
                                positionOffset: Float ,
                                positionOffsetPixels: Int) {

        indicatorTranslateX = items[position].rect.left + (positionOffset * itemWidth)

        invalidate()
    }

    override fun onPageSelected(position: Int) {
        activeItemIndex = position

    }


    private var _duration = DEFAULT_DURATION_ANIMATION
    private var duration get() = _duration
    set(value) {
        _duration = value
        invalidate() }

    private fun animate(position :Int){
        val left  =  items[position].rect.left
        ValueAnimator.ofFloat(indicatorTranslateX ,left).apply {
            this.duration = duration
            this.interpolator = AccelerateInterpolator()
            addUpdateListener {
                indicatorTranslateX = it.animatedValue as Float
                invalidate() }
            start() }
    }


    override fun onPageScrollStateChanged(state: Int) {


    }

                                            }