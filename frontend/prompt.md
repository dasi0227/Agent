如下修改
1. 所有 switchXxxStatus 和 xxxStatus 方法应该改为叫做 xxxToggle 方法
2. 所有 delete 和 toggle 方法的参数应该都直接是 String xxxId，不需要也不应该传递 Long 和 parseLong