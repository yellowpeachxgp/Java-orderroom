import com.all.search.allRoom;
import com.database.helper.DatabaseHelper;

import javax.swing.*;

/**
 * 房间统计功能测试
 */
public class test_room_stats {
    
    public static void main(String[] args) {
        System.out.println("=== 房间统计功能测试 ===");
        
        // 1. 测试数据库连接
        System.out.println("1. 测试数据库连接...");
        if (!DatabaseHelper.testConnection()) {
            System.out.println("❌ 数据库连接失败");
            return;
        }
        System.out.println("✅ 数据库连接正常");
        
        // 2. 启动房间统计界面
        System.out.println("\n2. 启动房间统计界面...");
        SwingUtilities.invokeLater(() -> {
            try {
                new allRoom();
                System.out.println("✅ 房间统计界面启动成功");
                System.out.println("请在界面中点击'刷新数据'按钮测试功能");
            } catch (Exception e) {
                System.out.println("❌ 房间统计界面启动失败: " + e.getMessage());
                e.printStackTrace();
            }
        });
        
        System.out.println("\n=== 测试说明 ===");
        System.out.println("1. 界面启动后，点击'刷新数据'按钮");
        System.out.println("2. 观察是否出现加载提示");
        System.out.println("3. 确认数据正常刷新而不卡死");
        System.out.println("4. 关闭界面测试完成");
    }
} 