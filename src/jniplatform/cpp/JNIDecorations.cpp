#include "JNIDecorations.h";
#include "com_weis_darklaf_platform_windows_JNIDecorations.h"
#include <dwmapi.h>
#include <iostream>
#include <map>
#include <winuser.h>

#define GWL_WNDPROC -4

std::map<HWND, WindowWrapper *> wrapper_map = std::map<HWND, WindowWrapper *>();

LRESULT HitTestNCA(HWND hWnd, WPARAM wParam, LPARAM lParam, WindowWrapper *wrapper)
{
    // Get the point coordinates for the hit test.
    POINT ptMouse = {GET_X_LPARAM(lParam), GET_Y_LPARAM(lParam)};

    // Get the window rectangle.
    RECT rcWindow;
    GetWindowRect(hWnd, &rcWindow);

    // Determine if the hit test is for resizing. Default middle (1,1).
    USHORT uRow = 1;
    USHORT uCol = 1;

    // Determine if the point is at the top or bottom of the window.
    if (ptMouse.y >= rcWindow.top && ptMouse.y < rcWindow.top + 5)
    {
        uRow = 0;
    }
    else if (ptMouse.y < rcWindow.bottom && ptMouse.y >= rcWindow.bottom - 5)
    {
        uRow = 2;
    }

    // Determine if the point is at the left or right of the window.
    if (ptMouse.x >= rcWindow.left && ptMouse.x < rcWindow.left + 5)
    {
        uCol = 0; // left side
    }
    else if (ptMouse.x < rcWindow.right && ptMouse.x >= rcWindow.right - 5)
    {
        uCol = 2; // right side
    }

    // Hit test (HTTOPLEFT, ... HTBOTTOMRIGHT)
    LRESULT hitTests[3][3] =
        {
            {HTTOPLEFT, HTTOP, HTTOPRIGHT},
            {HTLEFT, HTNOWHERE, HTRIGHT},
            {HTBOTTOMLEFT, HTBOTTOM, HTBOTTOMRIGHT},
        };
    LRESULT hit = hitTests[uRow][uCol];
    if (hit == HTNOWHERE || !wrapper->resizable)
    {
        //Handle window drag.
        if (ptMouse.y < rcWindow.top + wrapper->height && ptMouse.x >= rcWindow.left + wrapper->left && ptMouse.x <= rcWindow.right - wrapper->right)
        {
            return HTCAPTION;
        }
        return HTCLIENT;
    }
    else
    {
        return hit;
    }
}

LRESULT CALLBACK WindowWrapper::WindowProc(_In_ HWND hwnd, _In_ UINT uMsg, _In_ WPARAM wParam, _In_ LPARAM lParam)
{
    HWND handle = reinterpret_cast<HWND>(hwnd);
    auto wrapper = wrapper_map[handle];
    if (uMsg == WM_NCCALCSIZE)
    {
        if (wParam == TRUE)
        {
            SetWindowLong(hwnd, 0, 0);
            return TRUE;
        }
        return FALSE;
    }
    else if (uMsg == WM_NCHITTEST)
    {
        return HitTestNCA(hwnd, wParam, lParam, wrapper);
    }
    else if (uMsg == WM_PAINT)
    {
        PAINTSTRUCT ps;
        HDC hdc = BeginPaint(hwnd, &ps);

        RECT rc = ps.rcPaint;
        FillRect(hdc, &rc, CreateSolidBrush(wrapper->background));
        EndPaint(hwnd, &ps);
    }
    return CallWindowProc(wrapper->prev_proc, hwnd, uMsg, wParam, lParam);
}

JNIEXPORT void JNICALL
Java_com_weis_darklaf_platform_windows_JNIDecorations_setResizable(JNIEnv *env, jclass obj, jlong hwnd, jboolean res)
{
    HWND handle = reinterpret_cast<HWND>(hwnd);
    auto wrap = wrapper_map[handle];
    if (wrap)
    {
        wrap->resizable = res;
    }
}

JNIEXPORT void JNICALL
Java_com_weis_darklaf_platform_windows_JNIDecorations_updateValues(JNIEnv *env, jclass obj, jlong hwnd,
                                                                   jint l, jint r, jint h)
{
    HWND handle = reinterpret_cast<HWND>(hwnd);
    auto wrap = wrapper_map[handle];
    if (wrap)
    {
        wrap->left = l;
        wrap->right = r;
        wrap->height = h;
    }
}

JNIEXPORT void JNICALL
Java_com_weis_darklaf_platform_windows_JNIDecorations_setBackground(JNIEnv *env, jclass obj, jlong hwnd, jint r, jint g, jint b)
{
    HWND handle = reinterpret_cast<HWND>(hwnd);
    auto wrap = wrapper_map[handle];
    if (wrap)
    {
        wrap->background = RGB(r, g, b);
    }
}

JNIEXPORT void JNICALL
Java_com_weis_darklaf_platform_windows_JNIDecorations_installDecorations(JNIEnv *env, jclass obj, jlong hwnd)
{
    HWND handle = reinterpret_cast<HWND>(hwnd);

    MARGINS margins = {0, 0, 0, 1};
    DwmExtendFrameIntoClientArea(handle, &margins);
    SetWindowPos(handle, NULL, 0, 0, 0, 0, SWP_NOZORDER | SWP_NOOWNERZORDER | SWP_NOMOVE | SWP_NOSIZE | SWP_FRAMECHANGED);
    WNDPROC proc = reinterpret_cast<WNDPROC>(GetWindowLongPtr(handle, GWL_WNDPROC));

    WindowWrapper *wrapper = new WindowWrapper();
    wrapper->prev_proc = proc;
    wrapper_map[handle] = wrapper;

    SetWindowLongPtr((HWND)hwnd, GWL_WNDPROC, (LONG_PTR)WindowWrapper::WindowProc);
}

JNIEXPORT void JNICALL
Java_com_weis_darklaf_platform_windows_JNIDecorations_uninstallDecorations(JNIEnv *env, jclass obj, jlong hwnd)
{
    HWND handle = reinterpret_cast<HWND>(hwnd);
    auto wrap = wrapper_map[handle];
    if (wrap)
    {
        SetWindowLongPtr((HWND)hwnd, GWL_WNDPROC, reinterpret_cast<LONG_PTR>(wrap->prev_proc));
        wrapper_map.erase(handle);
        delete (wrap);
    }
}

//Window functions.

JNIEXPORT void JNICALL
Java_com_weis_darklaf_platform_windows_JNIDecorations_minimize(JNIEnv *env, jclass obj, jlong hwnd)
{
    HWND handle = reinterpret_cast<HWND>(hwnd);
    ShowWindow(handle, SW_MINIMIZE);
}

JNIEXPORT void JNICALL
Java_com_weis_darklaf_platform_windows_JNIDecorations_maximize(JNIEnv *env, jclass obj, jlong hwnd)
{
    HWND handle = reinterpret_cast<HWND>(hwnd);
    ShowWindow(handle, SW_MAXIMIZE);
}

JNIEXPORT void JNICALL
Java_com_weis_darklaf_platform_windows_JNIDecorations_restore(JNIEnv *env, jclass obj, jlong hwnd)
{
    HWND handle = reinterpret_cast<HWND>(hwnd);
    ShowWindow(handle, SW_RESTORE);
}