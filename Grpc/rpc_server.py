# -*- coding: UTF-8 -*-
from concurrent import futures
import grpc
import scan_pb2
import urllib
import scan_pb2_grpc
import yaml
import sys,eventlet
from Class_Poc import Class_Poc
sys.path.append('./modules')
class PocServicer(scan_pb2_grpc.pocServicer):
    def vuln_scan_python_by_souce_code(self,source_code,url):
        # print("【Scan11】【%s】【%s】【%s】"%("Python Poc",url,poc.get("detail").get("name")))
        eventlet.monkey_patch(thread=False, time=True)
        with eventlet.Timeout(int(120), False):
            try:
                # 创建一个干净的局部命名空间
                local_vars = {}
                # 创建一个全局命名空间
                global_vars = globals()
                # 执行代码
                try:
                    exec(source_code, global_vars, local_vars)
                except Exception as e:
                    print(f"Error executing code: {e}")
                    return  {"url":url,"name":"","result":False,"others":str(e)}
                vul_info = {"vuln_name":""}
                if 'vuln_info' in local_vars:
                    vul_info = local_vars['vuln_info']()
                    print("【Scan】【%s】【%s】【%s】"%("Python",url,vul_info.get("vuln_name")))
                if 'do_poc' in local_vars:
                    _url = urllib.parse.urlparse(url)
                    hostname = _url.hostname
                    port = _url.port
                    scheme = _url.scheme
                    result = local_vars['do_poc'](url, hostname, port, scheme, '')
                    result =  {"url":url,"name":vul_info.get("vuln_name"),"result":bool(result.get("Result")),"others":str(result.get("Result_Info"))}
                    return result
                else:
                    print("do_poc not found in local namespace")      
                    result =  {"url":url,"name":vul_info.get("vuln_name"),"result":False,"others":"未找到do_poc方法"}
                    return result
            except Exception as e:
                return  {"url":url,"name":vul_info.get("vuln_name"),"result":False,"others":str(e)}
    def vuln_scan_yaml_by_souce_code(self,poc,url):
        eventlet.monkey_patch(thread=False, time=True)
        with eventlet.Timeout(int(120), False):
            try:
                poc_obj  = Class_Poc(url,poc,120,False)
                result = poc_obj.main()
                return  {"url":url,"name":poc.get("detail").get("name"),"result":result.get("result"),"others":result.get("others")}
            except Exception as e:
                return  {"url":url,"name":poc.get("detail").get("name"),"result":False,"others":str(e)}
    def pocscan(self, request, context):
        url  = request.url
        poc_type = request.poc_type
        poc = request.poc
        # print(poc_type,url,poc)
        if poc_type == "Yaml":
            try:
                poc = yaml.safe_load(poc)
            except:
                print("【Error】【%s】【%s】【%s】 %s"%(poc_type,url,poc,"POC Yaml转换失败！"))
                return scan_pb2.Poc_Response(is_success=False, result="POC Yaml转换失败！")
            print("【Scan】【%s】【%s】【%s】"%(poc_type,url,poc.get("detail").get("name")))
            result   = self.vuln_scan_yaml_by_souce_code(poc,url)
            print("【Result】【%s】【%s】【%s】【%s】【%s】"%(poc_type,url,poc.get("detail").get("name"),result.get("result"),result.get("others")))
            return scan_pb2.Poc_Response(is_success=bool(result.get("result")), result=str(result.get("others")))
            
        elif poc_type == "Python":
            print("【Scan】【%s】【%s】【%s】"%(poc_type,url,poc))
            # result =  {"url":url,"name":vul_info.get("vuln_name"),"result":False,"others":"未找到do_poc方法"}
            result   = self.vuln_scan_python_by_souce_code(poc,url)
            if result.get("url"):
                url = result.get("url")
            print("【Result】【%s】【%s】【%s】【%s】【%s】"%(poc_type,url,result.get("name"),result.get("result"),result.get("others")))
            return scan_pb2.Poc_Response(is_success=bool(result.get("result")), result=str(result.get("others")))
        else:
            print("【Error】【%s】【%s】【%s】 %s"%(poc_type,url,poc,"POC插件类型错误！"))
            return scan_pb2.Poc_Response(is_success=False, result="POC插件类型错误！")
    # def check_python(source_code):
    #     # 读取文件内容
    #     with open("plugins.py", 'r',encoding='utf-8') as file:
    #         source_code = file.read()

    #     # 创建一个干净的局部命名空间
    #     local_vars = {}
        
    #     # 创建一个全局命名空间
    #     global_vars = globals()
        
    #     # 执行代码
    #     try:
    #         exec(source_code, global_vars, local_vars)
    #     except Exception as e:
    #         print(f"Error executing code: {e}")
    #         return
    #     if 'vuln_info' in local_vars:
    #         print(local_vars['vuln_info']())
    #     else:
    #         print("vuln_info not found in local namespace")            
def serve(host="127.0.0.1:23333"):
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=20000))
    scan_pb2_grpc.add_pocServicer_to_server(PocServicer(), server)
    server.add_insecure_port(host)
    server.start()
    print("【Server】Starting server. Listening on "+host)
    server.wait_for_termination()
if __name__ == '__main__':
    if(len(sys.argv))==1:
        serve()
    elif (len(sys.argv))==2:
        serve(sys.argv[1])
    else:
        print("Usage: python rpc_server.py [host:port]")
    print(len(sys.argv))
    # PocServicer.check_python("")