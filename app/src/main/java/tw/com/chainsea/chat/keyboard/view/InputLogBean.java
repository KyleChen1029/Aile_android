package tw.com.chainsea.chat.keyboard.view;

/**
 * current by evan on 2020-05-21
 *
 * @author Evan Wang
 * @date 2020-05-21
 */
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder(builderMethodName = "Build", toBuilder = true)
//public class InputLogBean {
//
//    @SerializedName("type")
//    private InputLogType type;
//    @Builder.Default
//    private boolean isTheme = false;
//    private String id;
//    private String text;
//
//
//    public String toJson() {
//        return JsonHelper.getInstance().toJson(this);
//    }
//
//    public static InputLogBean from(String json) {
//        if (Strings.isNullOrEmpty(json)) {
//            return InputLogBean.Build().id("").type(InputLogType.TEXT).text("").build();
//        }
//        try {
//            return JsonHelper.getInstance().from(json, InputLogBean.class);
//        }catch (JsonSyntaxException e) {
//            return InputLogBean.Build().id("").type(InputLogType.TEXT).text(json).build();
//        }
//    }
//
////      return InputLogBean.Build()
////              .id(id)
////                .type(type)
////                .text(text)
////                .build();
//}
