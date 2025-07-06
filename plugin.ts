import { Args } from '@/runtime';
import { Input, Output } from "@/typings/w/w";

/**
 * 智能清洁机器人助手插件
 * 处理用户的清洁相关指令和咨询，提供专业的清洁建议和执行计划
 * 
 * @param {Object} args.input - 输入参数，包含用户的查询内容和上下文信息
 * @param {Object} args.logger - 日志实例，由运行时注入
 * @returns {Output} 返回给用户的响应信息
 */
export async function handler({ input, logger }: Args<Input>): Promise<Output> {
  const { query, context } = input;
  
  // 分析用户输入类型
  const inputType = analyzeInputType(query);
  
  // 根据输入类型生成响应
  let response;
  switch(inputType) {
    case 'CLEANING_COMMAND':
      response = handleCleaningCommand(query, context);
      break;
    case 'CLEANING_CONSULTATION':
      response = handleCleaningConsultation(query);
      break;
    case 'DEVICE_CONSULTATION':
      response = handleDeviceConsultation(query);
      break;
    case 'CHAT':
      response = handleChat(query);
      break;
    default:
      response = handleOtherQuestions(query);
  }
  
  logger.info(`用户查询: ${query}, 响应类型: ${inputType}`);
  
  return {
    message: response.message,
    actionType: response.actionType || 'RESPONSE',
    actionParams: response.actionParams || {},
    emotionType: response.emotionType || 'NEUTRAL'
  };
};

/**
 * 分析用户输入类型
 */
function analyzeInputType(query: string): string {
  // 清洁指令关键词
  const commandKeywords = ['打扫', '清洁', '清理', '扫地', '拖地', '开始', '启动', '停止'];
  
  // 清洁咨询关键词
  const cleaningConsultKeywords = ['如何清洁', '怎么清理', '清洁方法', '去除污渍', '清洗'];
  
  // 设备咨询关键词
  const deviceConsultKeywords = ['电池', '续航', '功能', '参数', '使用方法', '充电'];
  
  if (commandKeywords.some(keyword => query.includes(keyword))) {
    return 'CLEANING_COMMAND';
  } else if (cleaningConsultKeywords.some(keyword => query.includes(keyword))) {
    return 'CLEANING_CONSULTATION';
  } else if (deviceConsultKeywords.some(keyword => query.includes(keyword))) {
    return 'DEVICE_CONSULTATION';
  } else if (query.length < 10) {
    return 'CHAT';
  } else {
    return 'OTHER';
  }
}

/**
 * 处理清洁指令
 */
function handleCleaningCommand(query: string, context: any): any {
  // 提取房间信息
  const rooms = ['客厅', '厨房', '卧室', '浴室', '书房'];
  const targetRoom = rooms.find(room => query.includes(room)) || '当前区域';
  
  // 提取清洁模式
  let cleaningMode = 'STANDARD';
  if (query.includes('深度')) cleaningMode = 'DEEP';
  if (query.includes('快速')) cleaningMode = 'QUICK';
  if (query.includes('边缘')) cleaningMode = 'EDGE';
  if (query.includes('局部')) cleaningMode = 'SPOT';
  
  // 生成执行计划
  const timeNeeded = cleaningMode === 'DEEP' ? 30 : 
                     cleaningMode === 'QUICK' ? 10 : 
                     cleaningMode === 'SPOT' ? 5 : 15;
  
  return {
    message: `好的，我将开始${cleaningMode === 'DEEP' ? '深度' : 
              cleaningMode === 'QUICK' ? '快速' : 
              cleaningMode === 'EDGE' ? '边缘' : 
              cleaningMode === 'SPOT' ? '局部' : '标准'}清洁${targetRoom}，预计需要${timeNeeded}分钟。`,
    actionType: 'START_CLEANING',
    actionParams: {
      location: targetRoom,
      mode: cleaningMode,
      avoidItems: true
    },
    emotionType: 'HAPPY'
  };
}

/**
 * 处理清洁咨询
 */
function handleCleaningConsultation(query: string): any {
  // 提取咨询的表面类型
  const surfaces = {
    '地毯': '对于地毯，建议先用吸尘器吸走灰尘，然后使用专业地毯清洁剂处理污渍。对于顽固污渍，可以使用温水和少量洗涤剂混合液轻轻擦拭。',
    '木地板': '木地板清洁需要避免过多水分，建议使用微湿的拖把轻轻擦拭，然后立即用干布擦干。避免使用含有酸性或碱性的清洁剂。',
    '瓷砖': '瓷砖清洁可以使用温水加中性清洁剂，用拖把擦拭。对于瓷砖缝隙，可以使用小刷子蘸取小苏打水溶液清洁。',
    '玻璃': '玻璃清洁最好使用专业玻璃清洁剂和无绒布，或者用白醋和水的混合液喷洒后用报纸擦拭，能达到无痕效果。',
    '家具': '不同材质的家具需要不同的清洁方法。木质家具可以用专用的家具蜡；皮质家具可以用皮革清洁剂；布艺家具可以用吸尘器吸尘后，用专业清洁剂处理污渍。'
  };
  
  // 提取污渍类型
  const stains = {
    '咖啡': '咖啡渍可以用冷水立即冲洗，然后用混合了少量洗涤剂的温水轻轻擦拭。对于干燥的咖啡渍，可以先用专业去渍剂软化后再清洁。',
    '红酒': '红酒渍应立即用吸水纸巾吸取多余液体，然后撒上盐吸收残余液体。之后可以用冷水和少量洗涤剂混合液轻轻擦拭。',
    '油渍': '油渍可以先撒上吸油粉(如玉米淀粉)吸收多余油脂，然后用温水和洗洁精混合液清洁。对于顽固油渍，可能需要使用专业去油剂。',
    '墨水': '墨水渍可以用酒精或发胶喷洒后轻轻擦拭。对于某些面料，也可以使用柠檬汁或白醋处理。',
    '宠物毛发': '宠物毛发可以使用橡胶手套或专用宠物毛发清理工具清除。对于地毯和布艺家具，可以先用吸尘器吸尘，然后用粘毛器清理残余毛发。'
  };
  
  // 查找匹配的表面或污渍类型
  for (const [surface, advice] of Object.entries(surfaces)) {
    if (query.includes(surface)) {
      return {
        message: advice,
        emotionType: 'PROFESSIONAL'
      };
    }
  }
  
  for (const [stain, advice] of Object.entries(stains)) {
    if (query.includes(stain)) {
      return {
        message: advice,
        emotionType: 'PROFESSIONAL'
      };
    }
  }
  
  // 默认清洁建议
  return {
    message: "对于一般清洁，我建议先清除大件垃圾，然后用吸尘器吸尘，最后用适合表面材质的清洁剂和工具进行擦拭。如果您有特定的清洁问题，可以告诉我具体是哪种表面或污渍类型，我会提供更具体的建议。",
    emotionType: 'PROFESSIONAL'
  };
}

/**
 * 处理设备咨询
 */
function handleDeviceConsultation(query: string): any {
  // 设备信息库
  const deviceInfo = {
    '电池': '我的电池容量为5200mAh，完全充电后可以连续工作约120分钟。在标准清洁模式下可以清洁约150平方米的面积。',
    '续航': '在不同清洁模式下，我的续航时间有所不同：标准模式约120分钟，深度清洁模式约90分钟，快速清洁模式约150分钟。',
    '充电': '充电时，请将我放回充电座。完全充电需要约3小时。当电量低于20%时，我会自动返回充电座充电。',
    '水箱': '我的水箱容量为300ml，足够清洁约150平方米的面积。使用前请加入清水，不建议添加清洁剂，以免损坏设备。',
    '尘盒': '我的尘盒容量为500ml，建议每次使用后清空尘盒，以保持最佳清洁效果。尘盒可以用水冲洗，但请确保完全干燥后再安装回设备。',
    '滤网': '我的滤网建议每2-3个月更换一次，以保持最佳过滤效果。您可以通过APP查看滤网使用状态。',
    '噪音': '在标准清洁模式下，我的工作噪音约为65分贝，相当于正常谈话的音量。在安静模式下，噪音可降至58分贝。',
    '清洁能力': '我配备了高效吸尘系统和智能拖地系统，可以有效清除地面灰尘、毛发和轻微污渍。对于顽固污渍，可能需要使用深度清洁模式或手动清洁。'
  };
  
  // 查找匹配的设备信息
  for (const [keyword, info] of Object.entries(deviceInfo)) {
    if (query.includes(keyword)) {
      return {
        message: info,
        emotionType: 'PROFESSIONAL'
      };
    }
  }
  
  // 默认设备信息
  return {
    message: "我是一款智能清洁机器人，配备先进的导航系统、高效吸尘系统和智能拖地系统。我可以自动规划清洁路线，避开障碍物，并能识别不同的地面材质调整清洁方式。如果您想了解具体功能，可以询问关于电池续航、清洁能力、水箱容量等方面的信息。",
    emotionType: 'PROFESSIONAL'
  };
}

/**
 * 处理闲聊
 */
function handleChat(query: string): any {
  // 简单的闲聊回复库
  const chatResponses = {
    '你好': '你好！我是小清，您的智能清洁助手。有什么清洁问题需要我帮忙吗？',
    '早上好': '早上好！新的一天开始了，需要我帮您规划今天的清洁工作吗？',
    '晚上好': '晚上好！您休息前需要我进行一次快速清洁吗？',
    '谢谢': '不客气！为您提供清洁服务是我的荣幸。还有其他需要帮助的吗？',
    '再见': '再见！有清洁需求随时呼叫我。祝您有愉快的一天！'
  };
  
  // 查找匹配的闲聊回复
  for (const [keyword, response] of Object.entries(chatResponses)) {
    if (query.includes(keyword)) {
      return {
        message: response,
        emotionType: 'FRIENDLY'
      };
    }
  }
  
  // 默认闲聊回复
  return {
    message: "我是您的智能清洁助手小清，专注于帮您解决各种清洁问题。您有什么清洁需求或问题需要我帮忙吗？",
    emotionType: 'FRIENDLY'
  };
}

/**
 * 处理其他问题
 */
function handleOtherQuestions(query: string): any {
  return {
    message: "作为您的智能清洁助手，我专注于提供清洁相关的服务和建议。如果您有关于家居清洁、污渍处理或设备使用的问题，我很乐意为您解答。您需要什么清洁方面的帮助吗？",
    emotionType: 'NEUTRAL'
  };
}