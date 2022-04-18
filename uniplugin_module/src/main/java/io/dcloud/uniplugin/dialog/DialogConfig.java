/*
 * Copyright (c) 2016-present 贵州纳雍穿青人李裕江<1032694760@qq.com>
 *
 * The software is licensed under the Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *     http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND, EITHER EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT, MERCHANTABILITY OR FIT FOR A PARTICULAR
 * PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

package io.dcloud.uniplugin.dialog;

import io.dcloud.uniplugin.dialog.DialogColor;
import io.dcloud.uniplugin.dialog.DialogStyle;

/**
 * @author 贵州山魈羡民 (1032694760@qq.com)
 * @since 2021/9/16 15:55
 */
public final class DialogConfig {
    private static int dialogStyle = io.dcloud.uniplugin.dialog.DialogStyle.Default;
    private static io.dcloud.uniplugin.dialog.DialogColor dialogColor = new io.dcloud.uniplugin.dialog.DialogColor();

    private DialogConfig() {
        super();
    }

    public static void setDialogStyle(@io.dcloud.uniplugin.dialog.DialogStyle int style) {
        dialogStyle = style;
    }

    @DialogStyle
    public static int getDialogStyle() {
        return dialogStyle;
    }

    public static void setDialogColor(io.dcloud.uniplugin.dialog.DialogColor color) {
        dialogColor = color;
    }

    public static io.dcloud.uniplugin.dialog.DialogColor getDialogColor() {
        if (dialogColor == null) {
            dialogColor = new DialogColor();
        }
        return dialogColor;
    }

}
