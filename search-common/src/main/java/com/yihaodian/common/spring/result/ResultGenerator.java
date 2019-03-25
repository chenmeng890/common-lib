package com.yihaodian.common.spring.result;

public class ResultGenerator {
	public static ResultBase getRightResult(){
		return ResultConstant.RIGHT_RESULT;
	}
	
//	public static<T> ResultBase getPagedListReult(String kind, List<T> list, int page_no, int page_size){
//		return new PagedListResult<T>(kind, list, page_no, page_size);
//	}
//	
//	public static<T> ResultBase getPagedListReultExactly(String kind, List<T> list, int page_no, int page_size, int total){		
//		PagedListData<T> pagedList = new PagedListData<T>(list, page_no, page_size, total);
//		PagedListResult<T> result = new PagedListResult<T>(kind, pagedList);
//		return result;
//	}
//	
//	public static<T> ResultBase getListResult(String kind, List<T> list){
//		return new ListResult<T>(kind, list);
//	}
//	
//	public static<T> ResultBase getPieceResult(String kind, T obj){
//		return new PieceResult<T>(kind, obj);
//	}

	public static<T> ResultBase getSysErrResult(){
		return ResultConstant.SYS_ERR_RESULT;
	}
	
	public static<T> ResultBase getNotFoundResult(){
		return ResultConstant.NOT_FOUND_RESULT;
	}
	
	public static<T> ResultBase getOverThreashold(){
		return ResultConstant.OVER_THREASHOLD_RESULT;
	}
	
	public static<T> ResultBase getUnprocessableResult(){
		return ResultConstant.UNPROCESSABLE_RESULT;
	}
	
	public static<T> ResultBase getUnauthorizedResult(){
		return ResultConstant.UNAUTHORIZED_RESULT;
	}
}
