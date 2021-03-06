/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific languag`e governing permissions and
 * limitations under the License.
 */

package android.support.smv7.widget;

import android.graphics.PointF;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

/**
 * Implementation of the {@link android.support.smv7.widget.SnapHelper} supporting snapping in either vertical or horizontal
 * orientation.
 * <p>
 * The implementation will snap the center of the target child view to the center of
 * the attached {@link android.support.smv7.widget.RecyclerView}. If you intend to change this behavior then override
 * {@link android.support.smv7.widget.SnapHelper#calculateDistanceToFinalSnap}.
 */
public class LinearSnapHelper extends SnapHelper {

    private static final float INVALID_DISTANCE = 1f;

    // Orientation helpers are lazily created per LayoutManager.
    @Nullable
    private android.support.smv7.widget.OrientationHelper mVerticalHelper;
    @Nullable
    private android.support.smv7.widget.OrientationHelper mHorizontalHelper;

    @Override
    public int[] calculateDistanceToFinalSnap(
            @NonNull android.support.smv7.widget.RecyclerView.LayoutManager layoutManager, @NonNull View targetView) {
        int[] out = new int[2];
        if (layoutManager.canScrollHorizontally()) {
            out[0] = distanceToCenter(layoutManager, targetView,
                    getHorizontalHelper(layoutManager));
        } else {
            out[0] = 0;
        }

        if (layoutManager.canScrollVertically()) {
            out[1] = distanceToCenter(layoutManager, targetView,
                    getVerticalHelper(layoutManager));
        } else {
            out[1] = 0;
        }
        return out;
    }

    @Override
    public int findTargetSnapPosition(android.support.smv7.widget.RecyclerView.LayoutManager layoutManager, int velocityX,
                                      int velocityY) {
        if (!(layoutManager instanceof android.support.smv7.widget.RecyclerView.SmoothScroller.ScrollVectorProvider)) {
            return android.support.smv7.widget.RecyclerView.NO_POSITION;
        }

        final int itemCount = layoutManager.getItemCount();
        if (itemCount == 0) {
            return android.support.smv7.widget.RecyclerView.NO_POSITION;
        }

        final View currentView = findSnapView(layoutManager);
        if (currentView == null) {
            return android.support.smv7.widget.RecyclerView.NO_POSITION;
        }

        final int currentPosition = layoutManager.getPosition(currentView);
        if (currentPosition == android.support.smv7.widget.RecyclerView.NO_POSITION) {
            return android.support.smv7.widget.RecyclerView.NO_POSITION;
        }

        android.support.smv7.widget.RecyclerView.SmoothScroller.ScrollVectorProvider vectorProvider =
                (android.support.smv7.widget.RecyclerView.SmoothScroller.ScrollVectorProvider) layoutManager;
        // deltaJumps sign comes from the velocity which may not match the order of children in
        // the LayoutManager. To overcome this, we ask for a vector from the LayoutManager to
        // get the direction.
        PointF vectorForEnd = vectorProvider.computeScrollVectorForPosition(itemCount - 1);
        if (vectorForEnd == null) {
            // cannot get a vector for the given position.
            return android.support.smv7.widget.RecyclerView.NO_POSITION;
        }

        int vDeltaJump, hDeltaJump;
        if (layoutManager.canScrollHorizontally()) {
            hDeltaJump = estimateNextPositionDiffForFling(layoutManager,
                    getHorizontalHelper(layoutManager), velocityX, 0);
            if (vectorForEnd.x < 0) {
                hDeltaJump = -hDeltaJump;
            }
        } else {
            hDeltaJump = 0;
        }
        if (layoutManager.canScrollVertically()) {
            vDeltaJump = estimateNextPositionDiffForFling(layoutManager,
                    getVerticalHelper(layoutManager), 0, velocityY);
            if (vectorForEnd.y < 0) {
                vDeltaJump = -vDeltaJump;
            }
        } else {
            vDeltaJump = 0;
        }

        int deltaJump = layoutManager.canScrollVertically() ? vDeltaJump : hDeltaJump;
        if (deltaJump == 0) {
            return android.support.smv7.widget.RecyclerView.NO_POSITION;
        }

        int targetPos = currentPosition + deltaJump;
        if (targetPos < 0) {
            targetPos = 0;
        }
        if (targetPos >= itemCount) {
            targetPos = itemCount - 1;
        }
        return targetPos;
    }

    @Override
    public View findSnapView(android.support.smv7.widget.RecyclerView.LayoutManager layoutManager) {
        if (layoutManager.canScrollVertically()) {
            return findCenterView(layoutManager, getVerticalHelper(layoutManager));
        } else if (layoutManager.canScrollHorizontally()) {
            return findCenterView(layoutManager, getHorizontalHelper(layoutManager));
        }
        return null;
    }

    private int distanceToCenter(@NonNull android.support.smv7.widget.RecyclerView.LayoutManager layoutManager,
            @NonNull View targetView, android.support.smv7.widget.OrientationHelper helper) {
        final int childCenter = helper.getDecoratedStart(targetView) +
                (helper.getDecoratedMeasurement(targetView) / 2);
        final int containerCenter;
        if (layoutManager.getClipToPadding()) {
            containerCenter = helper.getStartAfterPadding() + helper.getTotalSpace() / 2;
        } else {
            containerCenter = helper.getEnd() / 2;
        }
        return childCenter - containerCenter;
    }

    /**
     * Estimates a position to which SnapHelper will try to scroll to in response to a fling.
     *
     * @param layoutManager The {@link android.support.smv7.widget.RecyclerView.LayoutManager} associated with the attached
     *                      {@link android.support.smv7.widget.RecyclerView}.
     * @param helper        The {@link android.support.smv7.widget.OrientationHelper} that is created from the LayoutManager.
     * @param velocityX     The velocity on the x axis.
     * @param velocityY     The velocity on the y axis.
     *
     * @return The diff between the target scroll position and the current position.
     */
    private int estimateNextPositionDiffForFling(android.support.smv7.widget.RecyclerView.LayoutManager layoutManager,
                                                 android.support.smv7.widget.OrientationHelper helper, int velocityX, int velocityY) {
        int[] distances = calculateScrollDistance(velocityX, velocityY);
        float distancePerChild = computeDistancePerChild(layoutManager, helper);
        if (distancePerChild <= 0) {
            return 0;
        }
        int distance =
                Math.abs(distances[0]) > Math.abs(distances[1]) ? distances[0] : distances[1];
        return (int) Math.floor(distance / distancePerChild);
    }

    /**
     * Return the child view that is currently closest to the center of this parent.
     *
     * @param layoutManager The {@link android.support.smv7.widget.RecyclerView.LayoutManager} associated with the attached
     *                      {@link android.support.smv7.widget.RecyclerView}.
     * @param helper The relevant {@link android.support.smv7.widget.OrientationHelper} for the attached {@link android.support.smv7.widget.RecyclerView}.
     *
     * @return the child view that is currently closest to the center of this parent.
     */
    @Nullable
    private View findCenterView(android.support.smv7.widget.RecyclerView.LayoutManager layoutManager,
                                android.support.smv7.widget.OrientationHelper helper) {
        int childCount = layoutManager.getChildCount();
        if (childCount == 0) {
            return null;
        }

        View closestChild = null;
        final int center;
        if (layoutManager.getClipToPadding()) {
            center = helper.getStartAfterPadding() + helper.getTotalSpace() / 2;
        } else {
            center = helper.getEnd() / 2;
        }
        int absClosest = Integer.MAX_VALUE;

        for (int i = 0; i < childCount; i++) {
            final View child = layoutManager.getChildAt(i);
            int childCenter = helper.getDecoratedStart(child) +
                    (helper.getDecoratedMeasurement(child) / 2);
            int absDistance = Math.abs(childCenter - center);

            /** if child center is closer than previous closest, set it as closest  **/
            if (absDistance < absClosest) {
                absClosest = absDistance;
                closestChild = child;
            }
        }
        return closestChild;
    }

    /**
     * Computes an average pixel value to pass a single child.
     * <p>
     * Returns a negative value if it cannot be calculated.
     *
     * @param layoutManager The {@link android.support.smv7.widget.RecyclerView.LayoutManager} associated with the attached
     *                      {@link android.support.smv7.widget.RecyclerView}.
     * @param helper        The relevant {@link android.support.smv7.widget.OrientationHelper} for the attached
     *                      {@link android.support.smv7.widget.RecyclerView.LayoutManager}.
     *
     * @return A float value that is the average number of pixels needed to scroll by one view in
     * the relevant direction.
     */
    private float computeDistancePerChild(android.support.smv7.widget.RecyclerView.LayoutManager layoutManager,
                                          android.support.smv7.widget.OrientationHelper helper) {
        View minPosView = null;
        View maxPosView = null;
        int minPos = Integer.MAX_VALUE;
        int maxPos = Integer.MIN_VALUE;
        int childCount = layoutManager.getChildCount();
        if (childCount == 0) {
            return INVALID_DISTANCE;
        }

        for (int i = 0; i < childCount; i++) {
            View child = layoutManager.getChildAt(i);
            final int pos = layoutManager.getPosition(child);
            if (pos == android.support.smv7.widget.RecyclerView.NO_POSITION) {
                continue;
            }
            if (pos < minPos) {
                minPos = pos;
                minPosView = child;
            }
            if (pos > maxPos) {
                maxPos = pos;
                maxPosView = child;
            }
        }
        if (minPosView == null || maxPosView == null) {
            return INVALID_DISTANCE;
        }
        int start = Math.min(helper.getDecoratedStart(minPosView),
                helper.getDecoratedStart(maxPosView));
        int end = Math.max(helper.getDecoratedEnd(minPosView),
                helper.getDecoratedEnd(maxPosView));
        int distance = end - start;
        if (distance == 0) {
            return INVALID_DISTANCE;
        }
        return 1f * distance / ((maxPos - minPos) + 1);
    }

    @NonNull
    private android.support.smv7.widget.OrientationHelper getVerticalHelper(@NonNull android.support.smv7.widget.RecyclerView.LayoutManager layoutManager) {
        if (mVerticalHelper == null || mVerticalHelper.mLayoutManager != layoutManager) {
            mVerticalHelper = android.support.smv7.widget.OrientationHelper.createVerticalHelper(layoutManager);
        }
        return mVerticalHelper;
    }

    @NonNull
    private android.support.smv7.widget.OrientationHelper getHorizontalHelper(
            @NonNull RecyclerView.LayoutManager layoutManager) {
        if (mHorizontalHelper == null || mHorizontalHelper.mLayoutManager != layoutManager) {
            mHorizontalHelper = OrientationHelper.createHorizontalHelper(layoutManager);
        }
        return mHorizontalHelper;
    }
}
