import ApiUtils from './api';

class UserManagerService {

    /**
     * 根据条件查询所有的用户
     *
     * @param data 查询条件
     * @return 返回的分页结果集
     */
    public static findAllUser(data:any): Promise<any> {
        return ApiUtils.post("/manager/user/getAllUser", data).then((res) =>{
            return res
        }).catch((error: any) => {
            // 处理错误情况
            console.error("查询所有用户信息失败", error);
            return {}
        });
    }

    /**
     * 冻结用户
     * @param data 参数信息
     */
    public static freezeUser(userId:any): Promise<any> {
        return ApiUtils.get("/manager/user/freezeUser", {
                    userId
                }).catch((error: any) => {
                    // 处理错误情况s
                    console.error("冻结用户失败", error);
                });
    }

    /**
     * 解封用户
     * @param data 参数信息
     */
        public static unsealUser(userId:any): Promise<any> {
            return ApiUtils.get("/manager/user/unsealUser", {
                        userId
                    }).catch((error: any) => {
                        // 处理错误情况
                        console.error("解封用户失败", error);
                    });
        }
}

export default UserManagerService