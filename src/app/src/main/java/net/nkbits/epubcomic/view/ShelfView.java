package net.nkbits.epubcomic.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.GridView;

import net.androidcomics.acv.R;

/**
 * Created by nakayama on 5/15/16.
 */
public class ShelfView extends GridView{
    private Bitmap background;

    public ShelfView(Context context, AttributeSet attributes) {
        super(context, attributes);

        this.setFocusableInTouchMode(true);
        this.setClickable(false);

        background = BitmapFactory.decodeResource(context.getResources(), R.drawable.shelf_single);
        this.setFocusable(true);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        final int count = getChildCount();
        final int top = count > 0 ? getChildAt(0).getTop() : 0;
        final int shelfWidth = background.getWidth();
        final int shelfHeight = background.getHeight();
        final int width = getWidth();
        final int height = getHeight();

        if(background != null) {
            for (int x = 0; x < width; x += shelfWidth) {
                for (int y = top; y < height; y += shelfHeight) {
                    canvas.drawBitmap(background, x, y, null);
                }

                //This draws the top pixels of the shelf above the current one

                Rect source = new Rect(0, shelfHeight - top, shelfWidth, shelfHeight);
                Rect dest = new Rect(x, 0, x + shelfWidth, top );

                canvas.drawBitmap(background, source, dest, null);
            }
        }

        super.dispatchDraw(canvas);
    }
}
